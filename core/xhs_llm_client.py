# https://github.com/modelcontextprotocol/python-sdk/blob/main/examples/clients/simple-chatbot/mcp_simple_chatbot/main.py
import asyncio
import json
import logging
import os
import shutil
import traceback
from contextlib import AsyncExitStack
from typing import Any

import openai
from dotenv import load_dotenv
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client
from mcp.client.streamable_http import streamablehttp_client

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

class Configuration:
    """Manages configuration and environment variables for the MCP client."""

    def __init__(self) -> None:
        """Initialize configuration with environment variables."""
        self.load_env()
        self.api_key = os.getenv("LLM_API_KEY")
        self.base_url = os.getenv("OPENAI_BASE_URL")
        self.default_model = os.getenv("DEFAULT_MODEL", "claude-sonnet-4-20250514")

    @staticmethod
    def load_env() -> None:
        """Load environment variables from .env file."""
        load_dotenv()

    @staticmethod
    def load_config(file_path: str) -> dict[str, Any]:
        """Load server configuration from JSON file.

        Args:
            file_path: Path to the JSON configuration file.

        Returns:
            Dict containing server configuration.

        Raises:
            FileNotFoundError: If configuration file doesn't exist.
            JSONDecodeError: If configuration file is invalid JSON.
        """
        with open(file_path, "r") as f:
            return json.load(f)

    @property
    def llm_api_key(self) -> str:
        """Get the LLM API key.

        Returns:
            The API key as a string.

        Raises:
            ValueError: If the API key is not found in environment variables.
        """
        if not self.api_key:
            raise ValueError("LLM_API_KEY not found in environment variables")
        return self.api_key

    @property
    def openai_base_url(self) -> str:
        """Get the OpenAI base URL.

        Returns:
            The base URL as a string.

        Raises:
            ValueError: If the base URL is not found in environment variables.
        """
        if not self.base_url:
            raise ValueError("OPENAI_BASE_URL not found in environment variables")
        return self.base_url


class Server:
    """Manages MCP server connections and tool execution."""

    def __init__(self, name: str, config: dict[str, Any]) -> None:
        self.name: str = name
        self.config: dict[str, Any] = config
        self.stdio_context: Any | None = None
        self.session: ClientSession | None = None
        self._cleanup_lock: asyncio.Lock = asyncio.Lock()
        self.exit_stack: AsyncExitStack = AsyncExitStack()

    async def initialize(self) -> None:
        """Initialize the server connection."""
        server_type = self.config.get("type", "stdio")
        
        if server_type == "streamable_http":
            # Handle streamable_http type
            url = self.config.get("url")
            if not url:
                raise ValueError(f"URL is required for streamable_http server {self.name}")
            
            try:
                # 使用 AsyncExitStack 管理连接生命周期
                transport = await self.exit_stack.enter_async_context(streamablehttp_client(url))
                read, write, _ = transport
                session = await self.exit_stack.enter_async_context(ClientSession(read, write))
                await session.initialize()
                self.session = session
            except Exception as e:
                logging.error(f"Error initializing streamable_http server {self.name}: {e}")
                await self.cleanup()
                raise
        else:
            # Handle stdio type (default)
            command = shutil.which("npx") if self.config["command"] == "npx" else self.config["command"]
            if command is None:
                raise ValueError("The command must be a valid string and cannot be None.")

            server_params = StdioServerParameters(
                command=command,
                args=self.config["args"],
                env={**os.environ, **self.config["env"]} if self.config.get("env") else None,
            )
            try:
                stdio_transport = await self.exit_stack.enter_async_context(stdio_client(server_params))
                read, write = stdio_transport
                session = await self.exit_stack.enter_async_context(ClientSession(read, write))
                await session.initialize()
                self.session = session
            except Exception as e:
                logging.error(f"Error initializing server {self.name}: {e}")
                await self.cleanup()
                raise

    async def list_tools(self) -> list[Any]:
        """List available tools from the server.

        Returns:
            A list of available tools.

        Raises:
            RuntimeError: If the server is not initialized.
        """
        if not self.session:
            raise RuntimeError(f"Server {self.name} not initialized")

        tools_response = await self.session.list_tools()
        tools = []

        for item in tools_response:
            if isinstance(item, tuple) and item[0] == "tools":
                tools.extend(Tool(tool.name, tool.description, tool.inputSchema, tool.title) for tool in item[1])

        return tools

    async def execute_tool(
        self,
        tool_name: str,
        arguments: dict[str, Any],
        retries: int = 2,
        delay: float = 1.0,
    ) -> Any:
        """Execute a tool with retry mechanism.

        Args:
            tool_name: Name of the tool to execute.
            arguments: Tool arguments.
            retries: Number of retry attempts.
            delay: Delay between retries in seconds.

        Returns:
            Tool execution result.

        Raises:
            RuntimeError: If server is not initialized.
            Exception: If tool execution fails after all retries.
        """
        if not self.session:
            raise RuntimeError(f"Server {self.name} not initialized")

        attempt = 0
        while attempt < retries:
            try:
                logging.info(f"Executing {tool_name}...")
                result = await self.session.call_tool(tool_name, arguments)

                return result

            except Exception as e:
                attempt += 1
                logging.warning(f"Error executing tool: {e}. Attempt {attempt} of {retries}.")
                if attempt < retries:
                    logging.info(f"Retrying in {delay} seconds...")
                    await asyncio.sleep(delay)
                else:
                    logging.error("Max retries reached. Failing.")
                    raise

    async def cleanup(self) -> None:
        """Clean up server resources."""
        async with self._cleanup_lock:
            try:
                await self.exit_stack.aclose()
                self.session = None
                self.stdio_context = None
            except Exception as e:
                # 检查是否是 AsyncExitStack 的上下文错误（这是无害的）
                error_msg = str(e).lower()
                if "cancel scope" in error_msg or "different task" in error_msg:
                    # 这些错误是无害的，降级为 debug 日志
                    logging.debug(f"Cleanup context info for server {self.name}: {e}")
                else:
                    # 其他真正的错误才打印 error
                    logging.error(f"Error during cleanup of server {self.name}: {e}")


class Tool:
    """Represents a tool with its properties and formatting."""

    def __init__(
        self,
        name: str,
        description: str,
        input_schema: dict[str, Any],
        title: str | None = None,
    ) -> None:
        self.name: str = name
        self.title: str | None = title
        self.description: str = description
        self.input_schema: dict[str, Any] = input_schema

    def format_for_llm(self) -> str:
        """Format tool information for LLM.

        Returns:
            A formatted string describing the tool.
        """
        args_desc = []
        if "properties" in self.input_schema:
            for param_name, param_info in self.input_schema["properties"].items():
                arg_desc = f"- {param_name}: {param_info.get('description', 'No description')}"
                if param_name in self.input_schema.get("required", []):
                    arg_desc += " (required)"
                args_desc.append(arg_desc)

        # Build the formatted output with title as a separate field
        output = f"Tool: {self.name}\n"

        # Add human-readable title if available
        if self.title:
            output += f"User-readable title: {self.title}\n"

        output += f"""Description: {self.description}
            Arguments:
            {chr(10).join(args_desc)}
            """

        return output

    def to_openai_tool(self) -> dict:
        """Convert tool to OpenAI function calling format.
        
        Returns:
            A dictionary in OpenAI tool format.
        """
        # 如果type是object但properties为空，添加空的properties
        parameters = self.input_schema.copy()
        if (parameters.get("type") == "object" and 
            ("properties" not in parameters or not parameters["properties"])):
            parameters["properties"] = {}
            
        return {
            "type": "function",
            "function": {
                "name": self.name,
                "description": self.description,
                "parameters": parameters
            }
        }


class LLMClient:
    """Manages communication with the LLM provider."""

    def __init__(self, api_key: str, base_url: str, default_model: str = "claude-sonnet-4-20250514") -> None:
        # 豆包模型特殊处理：确保使用正确的base_url格式
        is_doubao = 'doubao' in default_model.lower() or '/ark.cn-beijing.volces.com/' in base_url
        if is_doubao:
            # 豆包模型使用的base_url不应包含/chat/completions，OpenAI客户端会自动添加
            if base_url.endswith('/chat/completions'):
                base_url = base_url[:-len('/chat/completions')]
        
        import openai
        self.client = openai.OpenAI(
            api_key=api_key,
            base_url=base_url
        )
        self.default_model = default_model
        self.is_doubao = is_doubao

    def get_tool_call_response(self, messages: list[dict[str, str]], tools: list = None, max_tokens: int = 32000):
        """Get a response from the LLM.
        
        Args:
            messages: A list of message dictionaries.
            tools: List of tools for function calling.
        
        Returns:
            The full response object from OpenAI.
        
        Raises:
            Exception: If the request to the LLM fails.
        """
        try:
            # 豆包模型特殊处理：调整消息格式和参数
            if self.is_doubao:
                # 对于豆包模型，确保messages格式正确
                processed_messages = self._process_doubao_messages(messages)
                
                # 豆包模型支持工具调用，转换tools格式并传递
                processed_tools = None
                if tools:
                    # 转换为豆包模型支持的工具格式
                    processed_tools = []
                    for tool in tools:
                        if isinstance(tool, Tool):
                            processed_tools.append(tool.to_openai_tool())
                        elif isinstance(tool, dict):
                            processed_tools.append(tool)
                
                # 记录请求参数
                request_params = {
                    'model': self.default_model,
                    'messages': processed_messages,
                    'temperature': 0.8,
                    'max_tokens': 4096,
                    'timeout': 120  # 增加超时时间到120秒，因为豆包模型响应较慢
                }
                
                # 发送请求，豆包模型支持tools参数
                logger.info(f"发送请求到豆包模型: {self.default_model}")
                logger.info(f"请求URL: {self.client.base_url}")
                logger.info(f"请求参数: model={self.default_model}, messages_length={len(processed_messages)}, temperature=0.8, max_tokens=4096")
                logger.info(f"是否包含tools: {processed_tools is not None}")
                if processed_tools:
                    logger.info(f"工具数量: {len(processed_tools)}")
                    for tool in processed_tools:
                        if isinstance(tool, dict) and 'function' in tool:
                            logger.info(f"工具名称: {tool['function']['name']}")
                
                try:
                    response = self.client.chat.completions.create(
                        model=self.default_model,
                        messages=processed_messages,
                        tools=processed_tools if processed_tools else None,
                        tool_choice="auto" if processed_tools else None,
                        temperature=0.8,
                        max_tokens=4096,  # 豆包模型需要明确设置max_tokens
                        timeout=120  # 增加超时时间到120秒，因为豆包模型响应较慢
                    )
                    logger.info(f"✅ 豆包模型请求成功，响应状态: 200")
                except Exception as e:
                    logger.error(f"❌ 豆包模型请求失败: {type(e).__name__}: {str(e)}")
                    logger.error(f"错误详情: {traceback.format_exc()}")
                    raise
                
                # 记录响应信息
                # logger.info(f"豆包模型响应状态: 成功")
                # logger.info(f"豆包模型响应: {json.dumps(response.model_dump(), ensure_ascii=False, indent=2)}")
                
                return response
            else:
                # 其他模型使用OpenAI客户端，添加超时设置
                response = self.client.chat.completions.create(
                    model=self.default_model,
                    messages=messages,
                    tools=tools,
                    tool_choice="auto" if tools else None,
                    temperature=0.8,
                    max_tokens=max_tokens,
                    timeout=30  # 添加30秒超时时间
                )
                return response

        except Exception as e:
            error_message = f"Error getting LLM response: {str(e)}"
            logging.error(error_message)
            # Return a mock response object for error cases
            class ErrorResponse:
                def __init__(self, error_msg):
                    self.choices = [type('obj', (object,), {
                        'message': type('obj', (object,), {
                            'content': error_msg,
                            'tool_calls': None
                        })()
                    })()]
            return ErrorResponse(f"I encountered an error: {error_message}. Please try again or rephrase your request.")

    def get_final_response(self, messages: list[dict[str, str]], tools: list = None, max_tokens: int = 32000):
        """Get a final response that summarizes tool call results and answers the original question.
        
        Args:
            messages: A list of message dictionaries including tool results.
            tools: List of tools for function calling (usually None for final response).
            max_tokens: Maximum tokens for the response.
        
        Returns:
            The full response object from OpenAI with summarized content.
        
        Raises:
            Exception: If the request to the LLM fails.
        """
        try:
            # Add a system message to guide the final response generation
            enhanced_messages = messages.copy()
            
            # Find the original user question from the messages
            original_question = None
            for msg in messages:
                if msg.get("role") == "user":
                    original_question = msg.get("content")
                    break
            
            # Add guidance for final response with decision making
            summary_prompt = f"""
                Based on the tool execution results above, you need to decide whether to continue with more tool calls or provide a final summary for the original question: "{original_question}"
    
                Please analyze the current information and decide:
    
                OPTION 1 - If you need more information to complete the task:
                - Call additional tools to gather missing information
                - Use tools for deeper research or verification
                - Continue the investigation process
    
                OPTION 2 - If you have sufficient information（This is the main task）:
                - Provide a comprehensive and well-structured final response
                - Synthesize all tool results into a coherent answer
                - Organize the information logically and make it actionable
                - Include all relevant details from the tool outputs
                - Use a professional and helpful tone
    
                Make your decision based on whether the current information is sufficient to fully answer the original question and complete the task requirements.
            """
            
            enhanced_messages.append({
                "role": "system", 
                "content": summary_prompt
            })
            
            # 豆包模型特殊处理：调整消息格式和参数
            if self.is_doubao:
                # 对于豆包模型，确保messages格式正确
                processed_messages = self._process_doubao_messages(enhanced_messages)
                
                logger.info(f"请求豆包模型生成最终响应，模型: {self.default_model}，消息数: {len(processed_messages)}")
                logger.info(f"处理后的豆包模型消息: {json.dumps(processed_messages, ensure_ascii=False, indent=2)}")
                
                # 记录请求参数
                request_params = {
                    'model': self.default_model,
                    'messages': processed_messages,
                    'temperature': 0.3,
                    'max_tokens': 4096,
                    'timeout': 120  # 增加超时时间到120秒，因为豆包模型响应较慢
                }
                logger.info(f"请求豆包模型最终响应参数: {json.dumps(request_params, ensure_ascii=False, indent=2)}")
                
                # 记录请求URL
                logger.info(f"请求豆包模型URL: {self.client.base_url}")
                
                # 发送请求
                response = self.client.chat.completions.create(
                    model=self.default_model,
                    messages=processed_messages,
                    temperature=0.3,  # Lower temperature for more focused response
                    max_tokens=4096,  # 豆包模型需要明确设置max_tokens
                    timeout=120  # 增加超时时间到120秒，因为豆包模型响应较慢
                )
                
                # 记录响应信息
                logger.info(f"豆包模型最终响应状态: 成功")
                logger.info(f"豆包模型最终响应: {json.dumps(response.model_dump(), ensure_ascii=False, indent=2)}")
                
                return response
            else:
                # 其他模型使用OpenAI客户端，添加超时设置
                response = self.client.chat.completions.create(
                    model=self.default_model,
                    messages=enhanced_messages,
                    tools=tools,  # Allow tools for continuation if needed
                    tool_choice="auto" if tools else None,
                    temperature=0.3,  # Lower temperature for more focused response
                    max_tokens=max_tokens,
                    timeout=30  # 添加30秒超时时间
                )
                return response

        except Exception as e:
            error_message = f"Error getting final LLM response: {str(e)}"
            logging.error(error_message)
            # Return a mock response object for error cases
            class ErrorResponse:
                def __init__(self, error_msg):
                    self.choices = [type('obj', (object,), {
                        'message': type('obj', (object,), {
                            'content': error_msg,
                            'tool_calls': None
                        })()
                    })()]
            return ErrorResponse(f"I encountered an error while generating the final response: {error_message}. Please try again.")
            
    def _process_doubao_messages(self, messages: list[dict[str, str]]):
        """处理豆包模型的消息格式
        
        Args:
            messages: 原始消息列表
            
        Returns:
            处理后的消息列表，适合豆包模型
        """
        processed = []
        
        # 豆包模型支持system角色，直接处理每个消息的content格式
        for msg in messages:
            processed_msg = msg.copy()
            
            # 豆包模型对content字段有特殊要求
            if 'content' in processed_msg:
                content = processed_msg['content']
                if isinstance(content, str):
                    # 如果content是字符串，转换为豆包模型支持的格式
                    processed_msg['content'] = [{'type': 'text', 'text': content}]
                elif isinstance(content, list):
                    # 如果content已经是列表，确保每个元素都有正确的格式
                    formatted_content = []
                    for item in content:
                        if isinstance(item, dict):
                            # 如果是字典，确保有type和text属性
                            if 'type' not in item or 'text' not in item:
                                # 如果缺少必要属性，尝试提取文本并重新格式化
                                text = item.get('text', '')
                                if not text:
                                    # 尝试从其他字段获取文本
                                    text = str(item.get('content', '') if 'content' in item else item)
                                formatted_content.append({'type': 'text', 'text': text})
                            else:
                                # 格式正确，直接添加
                                formatted_content.append(item)
                        elif isinstance(item, str):
                            # 如果是字符串，转换为正确格式
                            formatted_content.append({'type': 'text', 'text': item})
                        else:
                            # 其他类型，转换为字符串处理
                            formatted_content.append({'type': 'text', 'text': str(item)})
                    processed_msg['content'] = formatted_content
                else:
                    # 其他类型，转换为字符串处理
                    processed_msg['content'] = [{'type': 'text', 'text': str(content)}]
            
            processed.append(processed_msg)
        
        logger.debug(f"处理后的豆包消息: 原消息数={len(messages)}, 处理后消息数={len(processed)}")
        logger.debug(f"处理后的豆包消息详情: {processed}")
        return processed
