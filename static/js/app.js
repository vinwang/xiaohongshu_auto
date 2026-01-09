/**
 * Creator - Premium AI Content Dashboard
 * Logic Controller
 */

const API_BASE = '/api';

// --- 初始化 ---
document.addEventListener('DOMContentLoaded', () => {
    initBackgroundCanvas();
    initNavigation();
    loadConfig(); // 预加载配置

    // 绑定快捷键
    document.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            if (document.activeElement.id === 'topic-input') {
                startGenerate();
            }
        }
    });
});

// --- 视觉效果: 动态背景 Canvas ---
function initBackgroundCanvas() {
    const canvas = document.getElementById('bg-canvas');
    const ctx = canvas.getContext('2d');

    let width, height;
    let particles = [];

    function resize() {
        width = window.innerWidth;
        height = window.innerHeight;
        canvas.width = width;
        canvas.height = height;
        initParticles();
    }

    function initParticles() {
        particles = [];
        const count = Math.floor(width * height / 15000); // 根据屏幕面积决定粒子数
        for (let i = 0; i < count; i++) {
            particles.push({
                x: Math.random() * width,
                y: Math.random() * height,
                vx: (Math.random() - 0.5) * 0.5,
                vy: (Math.random() - 0.5) * 0.5,
                size: Math.random() * 200 + 50,
                hue: Math.random() * 60 + 220, // Blue-Purple range
                opacity: Math.random() * 0.3
            });
        }
    }

    function animate() {
        ctx.clearRect(0, 0, width, height);

        // 绘制渐变背景
        const gradient = ctx.createLinearGradient(0, 0, width, height);
        gradient.addColorStop(0, '#0f0f13');
        gradient.addColorStop(1, '#1a1a2e');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, width, height);

        // 绘制流体粒子
        particles.forEach(p => {
            p.x += p.vx;
            p.y += p.vy;

            if (p.x < -p.size) p.x = width + p.size;
            if (p.x > width + p.size) p.x = -p.size;
            if (p.y < -p.size) p.y = height + p.size;
            if (p.y > height + p.size) p.y = -p.size;

            const g = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.size);
            g.addColorStop(0, `hsla(${p.hue}, 80%, 60%, ${p.opacity})`);
            g.addColorStop(1, 'transparent');

            ctx.fillStyle = g;
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
            ctx.fill();
        });

        requestAnimationFrame(animate);
    }

    window.addEventListener('resize', resize);
    resize();
    animate();
}

// --- 导航与视图切换 ---
function initNavigation() {
    const navItems = document.querySelectorAll('.nav-item[data-target]');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const targetId = item.dataset.target;
            switchView(targetId);

            // 更新激活状态
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

function switchView(viewId) {
    // 隐藏所有视图
    document.querySelectorAll('.view-section').forEach(el => {
        el.classList.remove('active');
    });

    // 显示目标视图
    const targetView = document.getElementById(`view-${viewId}`);
    if (targetView) {
        targetView.classList.add('active');
    }
}

// --- 模态框管理 ---
function openModal(modalId) {
    const overlay = document.getElementById('modal-overlay');
    const modal = document.getElementById(`modal-${modalId}`);

    // 隐藏其他模态框
    document.querySelectorAll('.modal-glass').forEach(el => el.classList.remove('active'));

    overlay.classList.add('active');
    modal.classList.add('active');

    if (modalId === 'history') {
        loadTaskHistory();
    }
}

function closeModal() {
    const overlay = document.getElementById('modal-overlay');
    overlay.classList.remove('active');
    document.querySelectorAll('.modal-glass').forEach(el => el.classList.remove('active'));
}

// 点击遮罩层关闭
document.getElementById('modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'modal-overlay') {
        closeModal();
    }
});

// --- 核心功能: 生成内容 ---
async function startGenerate(initialTopic = null, initialContentType = null, existingTaskId = null) {
    console.log('startGenerate called with:', initialTopic, initialContentType, existingTaskId);
    const topicInput = document.getElementById('topic-input');

    let topic;
    if (initialTopic) {
        topic = initialTopic;
    } else {
        topic = topicInput.value.trim();
    }

    let contentType;
    if (initialContentType) {
        contentType = initialContentType;
    } else {
        contentType = document.querySelector('input[name="content-type"]:checked').value;
    }

    if (!topic) {
        showToast('请输入创作主题', 'info');
        if (!initialTopic) topicInput.focus();
        return;
    }

    // 创建任务卡片 UI
    let taskId = existingTaskId;
    if (!taskId) {
        taskId = 'task-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9); // Ensure unique ID
    }
    createTaskStatusCard(taskId, topic);

    // 仅在手动输入时清空输入框
    if (!initialTopic) {
        topicInput.value = '';
    }

    try {
        updateTaskProgress(taskId, 10, '正在启动创作引擎...');

        const response = await fetch(`${API_BASE}/generate-and-publish`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                topic,
                content_type: contentType,
                task_id: existingTaskId // 传递现有任务ID（如果是重试）
            })
        });

        // 模拟进度 (真实进度需 WebSocket，此处为模拟体验)
        simulateProgress(taskId);

        // 处理响应
        let responseData;
        try {
            responseData = await response.json();
        } catch (jsonError) {
            // 如果响应不是JSON格式，直接获取文本内容
            const textData = await response.text();
            throw new Error(textData || '服务器返回了无效的JSON响应');
        }

        if (response.ok && responseData.success) {
            // 检查发布状态
            if (responseData.publish_status === '已成功发布') {
                updateTaskProgress(taskId, 100, '发布成功！');
                showToast('内容创作完成', 'success');
            } else {
                // 发布失败但内容生成成功
                updateTaskStatus(taskId, 'error', '发布失败');
                // 只显示简短的错误信息，避免重复
                showToast('内容生成成功，但发布到小红书失败', 'error');
            }

            // 延迟展示结果
            setTimeout(() => {
                showResultModal(responseData);
            }, 1000);
        } else {
            // 提取详细的错误信息
            let errorMessage = responseData.error || responseData.detail || '生成失败';
            
            // 如果是字符串，直接使用
            if (typeof errorMessage === 'string') {
                updateTaskStatus(taskId, 'error', errorMessage);
                showToast(errorMessage, 'error');
            } else if (typeof errorMessage === 'object') {
                // 如果是对象，转换为字符串
                const errorStr = JSON.stringify(errorMessage, null, 2);
                updateTaskStatus(taskId, 'error', errorStr);
                showToast('生成失败，请查看详细错误信息', 'error');
            }
        }
    } catch (error) {
        // 显示完整的错误信息，包括发布失败的详细原因
        const fullErrorMessage = error.message || '请求失败';
        updateTaskStatus(taskId, 'error', fullErrorMessage);
        showToast(`请求失败: ${fullErrorMessage}`, 'error');
    }
}

function createTaskStatusCard(taskId, topic) {
    const container = document.getElementById('current-task-container');
    container.innerHTML = `
        <div id="${taskId}" class="status-card">
            <div class="status-header">
                <span class="status-topic">${topic}</span>
                <div class="status-actions">
                    <span class="status-badge running">进行中</span>
                    <!-- 重试按钮移到右上角 -->
                    <button class="btn-primary-glass retry-btn" onclick="retryTaskFromCard('${taskId}')" style="display: none;">
                        <span class="icon">🔄</span> 重试
                    </button>
                </div>
            </div>
            <div class="progress-track">
                <div class="progress-fill" style="width: 0%"></div>
            </div>
            <div class="status-text">准备就绪</div>
        </div>
    `;
}

function updateTaskProgress(taskId, percent, text) {
    const card = document.getElementById(taskId);
    if (!card) return;

    card.querySelector('.progress-fill').style.width = `${percent}%`;
    card.querySelector('.status-text').textContent = text;
}



// 更新任务状态时的处理
function updateTaskStatus(taskId, status, message) {
    const card = document.getElementById(taskId);
    if (!card) return;

    const badge = card.querySelector('.status-badge');
    badge.className = `status-badge ${status}`;
    badge.textContent = status === 'error' ? '失败' : '完成';

    card.querySelector('.status-text').textContent = message;
    
    if (status === 'error') {
        // 显示右上角重试按钮
        const retryBtn = card.querySelector('.retry-btn');
        retryBtn.style.display = 'inline-flex';
    }
}

// 从卡片重试任务
function retryTaskFromCard(taskId) {
    const card = document.getElementById(taskId);
    if (!card) return;
    
    const topic = card.querySelector('.status-topic').textContent;
    const contentType = document.querySelector('input[name="content-type"]:checked').value;
    
    // 清空当前任务容器，重新开始生成
    const container = document.getElementById('current-task-container');
    container.innerHTML = '';
    
    startGenerate(topic, contentType, taskId);
}

async function simulateProgress(taskId) {
    // 显示真实的执行步骤
    const steps = [
        { p: 30, t: '正在全网检索相关资料...' },
        { p: 50, t: 'AI 正在深度阅读与分析...' },
        { p: 70, t: '正在撰写与润色文案...' },
        { p: 90, t: '正在生成配图并发布...' }
    ];

    for (const step of steps) {
        await new Promise(r => setTimeout(r, 1000));
        const card = document.getElementById(taskId);
        // 如果任务已经结束（比如报错了），就不再更新
        if (!card || card.querySelector('.status-badge').classList.contains('error')) break;
        updateTaskProgress(taskId, step.p, step.t);
    }
}

// --- 结果展示 ---
function showResultModal(data) {
    document.getElementById('res-title').textContent = data.title || '无标题';
    document.getElementById('res-time').textContent = data.publish_time || new Date().toLocaleString();
    document.getElementById('res-content').textContent = data.content || '';

    // Tags
    const tagsContainer = document.getElementById('res-tags');
    tagsContainer.innerHTML = (data.tags || []).map(tag => `<span class="tag-glass">#${tag}</span>`).join('');

    // Images
    const imgContainer = document.getElementById('res-images');
    imgContainer.innerHTML = (data.images || []).map(url => `<img src="${url}" onclick="window.open('${url}')">`).join('');

    // 显示发布状态
    const statusContainer = document.getElementById('res-status');
    if (data.publish_status === '已成功发布') {
        statusContainer.innerHTML = '<span class="status-success">发布成功</span>';
        // 隐藏重试按钮
        document.getElementById('res-retry-btn').style.display = 'none';
    } else {
        statusContainer.innerHTML = `<span class="status-error">发布失败</span>`;
        // 显示重试按钮
        document.getElementById('res-retry-btn').style.display = 'block';
        // 保存当前数据到全局变量，用于重试
        window.currentContentData = data;
    }

    openModal('result');
}

// 重试发布
async function retryPublish() {
    if (!window.currentContentData) return;
    
    const data = window.currentContentData;
    const retryBtn = document.getElementById('res-retry-btn');
    const originalText = retryBtn.innerHTML;
    
    // 显示加载状态
    retryBtn.innerHTML = '<span class="icon">⏳</span> 重试中...';
    retryBtn.disabled = true;
    
    try {
        // 调用重试发布API
        const response = await fetch(`${API_BASE}/retry-publish`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: data.title,
                content: data.content,
                tags: data.tags,
                images: data.images
            })
        });
        
        const responseData = await response.json();
        
        if (response.ok && responseData.success) {
            showToast('发布成功', 'success');
            // 更新结果模态框中的状态
            document.getElementById('res-status').innerHTML = '<span class="status-success">发布成功</span>';
            retryBtn.style.display = 'none';
        } else {
            showToast('发布失败: ' + (responseData.error || '未知错误'), 'error');
            // 恢复按钮状态
            retryBtn.innerHTML = originalText;
            retryBtn.disabled = false;
        }
    } catch (error) {
        showToast('发布失败: ' + error.message, 'error');
        // 恢复按钮状态
        retryBtn.innerHTML = originalText;
        retryBtn.disabled = false;
    }
}

// --- 热点发现 ---
let selectedTopics = new Set();

async function fetchTrendingTopicsByDomain(domain) {
    // 更新 Tab 状态
    document.querySelectorAll('.tab-glass').forEach(el => {
        el.classList.toggle('active', el.textContent.includes(domain));
    });

    const grid = document.getElementById('trending-grid');
    grid.innerHTML = '<div class="empty-state-glass"><span class="icon">⏳</span><p>正在搜寻全网热点...</p></div>';

    try {
        const response = await fetch(`${API_BASE}/fetch-trending-topics`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ domain })
        });

        const data = await response.json();
        if (data.success && data.topics) {
            renderTrendingCards(data.topics);
        } else {
            grid.innerHTML = '<div class="empty-state-glass"><p>未找到相关热点</p></div>';
        }
    } catch (error) {
        showToast('获取热点失败', 'error');
        grid.innerHTML = '<div class="empty-state-glass"><p>加载失败，请重试</p></div>';
    }
}

async function fetchTopicsFromUrl() {
    const url = document.getElementById('url-input').value.trim();
    if (!url) return showToast('请输入链接', 'info');

    const grid = document.getElementById('trending-grid');
    grid.innerHTML = '<div class="empty-state-glass"><span class="icon">🕷️</span><p>正在爬取并分析网页...</p></div>';

    try {
        const response = await fetch(`${API_BASE}/fetch-topics-from-url`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ url })
        });

        const data = await response.json();
        if (data.success && data.topics) {
            renderTrendingCards(data.topics);
        }
    } catch (error) {
        showToast('提取失败', 'error');
    }
}

function renderTrendingCards(topics) {
    const grid = document.getElementById('trending-grid');
    grid.innerHTML = '';
    selectedTopics.clear();
    updateBatchActionState();
    updateSelectAllButtonState();

    currentTopics = topics; // 保存当前主题列表

    topics.forEach((topic, index) => {
        const card = document.createElement('div');
        card.className = 'topic-card';
        card.innerHTML = `
            <div class="card-check">✓</div>
            <div class="card-title">${topic.title}</div>
            <div class="card-summary">${topic.summary}</div>
        `;

        card.onclick = () => toggleTopicSelection(card, topic);
        grid.appendChild(card);

        // 动画入场
        card.style.animation = `slideInUp 0.5s ease backwards ${index * 0.05}s`;
    });

    // 显示全选工具栏
    const toolbar = document.getElementById('topic-toolbar');
    if (topics.length > 0) {
        toolbar.style.display = 'flex';
    } else {
        toolbar.style.display = 'none';
    }
}

function toggleTopicSelection(card, topic) {
    if (selectedTopics.has(topic)) {
        selectedTopics.delete(topic);
        card.classList.remove('selected');
    } else {
        selectedTopics.add(topic);
        card.classList.add('selected');
    }
    updateBatchActionState();
    updateSelectAllButtonState();
}

// 当前渲染的主题列表（用于全选）
let currentTopics = [];

function toggleSelectAll() {
    const cards = document.querySelectorAll('.topic-card');
    const allSelected = selectedTopics.size === currentTopics.length && currentTopics.length > 0;

    if (allSelected) {
        // 取消全选
        selectedTopics.clear();
        cards.forEach(card => card.classList.remove('selected'));
    } else {
        // 全选
        currentTopics.forEach(topic => selectedTopics.add(topic));
        cards.forEach(card => card.classList.add('selected'));
    }
    updateBatchActionState();
    updateSelectAllButtonState();
}

function updateSelectAllButtonState() {
    const btn = document.getElementById('btn-select-all');
    if (!btn) return;

    const allSelected = selectedTopics.size === currentTopics.length && currentTopics.length > 0;
    if (allSelected) {
        btn.innerHTML = '<span class="icon">✕</span> 取消全选';
    } else {
        btn.innerHTML = '<span class="icon">✓</span> 全选所有';
    }
}

function updateBatchActionState() {
    const bar = document.getElementById('batch-actions');
    const count = selectedTopics.size;

    document.getElementById('selected-count').textContent = `已选 ${count} 项`;

    if (count > 0) {
        bar.style.display = 'flex';
    } else {
        bar.style.display = 'none';
    }
}

async function batchGenerate() {
    if (selectedTopics.size === 0) return;

    const topics = Array.from(selectedTopics).map(t => t.title);
    console.log('Batch generating topics:', topics);
    showToast(`开始批量生成 ${topics.length} 个任务`, 'success');

    // 切换到创作中心查看进度
    switchView('home');

    // 获取当前选中的内容类型，用于批量任务
    const currentContentType = document.querySelector('input[name="content-type"]:checked').value;

    // 循环执行生成任务
    for (const topic of topics) {
        // 稍微延迟，避免 ID 冲突和 UI 拥挤
        await new Promise(r => setTimeout(r, 300));
        startGenerate(topic, currentContentType);
    }

    // 清空选择状态
    selectedTopics.clear();
    document.querySelectorAll('.topic-card.selected').forEach(c => c.classList.remove('selected'));
    updateBatchActionState();
    updateSelectAllButtonState();
}

// --- 配置管理 --- 

// AI平台默认配置
const PLATFORM_DEFAULTS = {
    openai: {
        base_url: 'https://api.openai.com/v1',
        model: 'gpt-4o'
    },
    claude: {
        base_url: 'https://api.anthropic.com/v1',
        model: 'claude-3-5-sonnet-20240620'
    },
    gemini: {
        base_url: 'https://generativelanguage.googleapis.com/v1',
        model: 'gemini-pro'
    },
    tongyi: {
        base_url: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
        model: 'qwen-plus'
    },
    wenxin: {
        base_url: 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat',
        model: 'ernie-3.5-8k'
    },
    doubao: {
        base_url: 'https://ark.cn-beijing.volces.com/api/v3',
        model: 'doubao-seed-1-8-251228'
    }
};

// 更新平台配置
function updatePlatformConfig() {
    const platform = document.getElementById('ai_platform').value;
    const defaults = PLATFORM_DEFAULTS[platform];
    
    if (defaults) {
        // 只在用户没有手动修改过模型名称时才更新默认值
        const modelInput = document.getElementById('default_model');
        if (!modelInput.value || modelInput.value === PLATFORM_DEFAULTS[platform === 'doubao' ? 'doubao' : platform].model) {
            document.getElementById('openai_base_url').value = defaults.base_url;
            document.getElementById('default_model').value = defaults.model;
            
            // 提示用户
            showToast(`已更新为${platform}平台默认配置`, 'info');
        } else {
            // 只更新Base URL，保留用户手动设置的模型名称
            document.getElementById('openai_base_url').value = defaults.base_url;
            showToast(`已更新为${platform}平台默认Base URL，保留当前模型名称`, 'info');
        }
    }
}
async function loadConfig() {
    try {
        const res = await fetch(`${API_BASE}/config`);
        const data = await res.json();
        if (data.success && data.config) {
            const c = data.config;
            if (c.ai_platform) document.getElementById('ai_platform').value = c.ai_platform;
            if (c.llm_api_key) document.getElementById('llm_api_key').placeholder = '已配置 (******)';
            if (c.openai_base_url) document.getElementById('openai_base_url').value = c.openai_base_url;
            if (c.default_model) document.getElementById('default_model').value = c.default_model;
            if (c.xhs_mcp_url) document.getElementById('xhs_mcp_url').value = c.xhs_mcp_url;
        }
    } catch (e) {
        console.error('Config load failed', e);
    }
}

async function saveConfig() {
    const config = {
        ai_platform: document.getElementById('ai_platform').value,
        llm_api_key: document.getElementById('llm_api_key').value,
        openai_base_url: document.getElementById('openai_base_url').value,
        default_model: document.getElementById('default_model').value,
        xhs_mcp_url: document.getElementById('xhs_mcp_url').value,
        jina_api_key: document.getElementById('jina_api_key').value,
        tavily_api_key: document.getElementById('tavily_api_key').value
    };

    try {
        const res = await fetch(`${API_BASE}/config`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(config)
        });
        const data = await res.json();
        if (data.success) {
            showToast('配置已保存', 'success');
            closeModal();
        }
    } catch (e) {
        showToast('保存失败', 'error');
    }
}

async function validateModel() {
    const status = document.getElementById('model-status');
    status.textContent = '验证中...';
    status.style.color = 'var(--color-text-muted)';

    try {
        const res = await fetch(`${API_BASE}/validate-model`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                llm_api_key: document.getElementById('llm_api_key').value,
                openai_base_url: document.getElementById('openai_base_url').value,
                model_name: document.getElementById('default_model').value
            })
        });
        const data = await res.json();
        if (data.success) {
            status.textContent = '✓ 可用';
            status.style.color = '#4caf50';
        } else {
            status.textContent = '✗ 不可用';
            status.style.color = '#f44336';
        }
    } catch (e) {
        status.textContent = '验证出错';
        status.style.color = '#f44336';
    }
}

// --- 历史记录 ---
// --- 历史记录 ---
let allHistoryData = [];

async function loadTaskHistory() {
    const list = document.getElementById('history-list');
    list.innerHTML = '<div style="text-align:center;padding:20px;">加载中...</div>';

    try {
        const res = await fetch(`${API_BASE}/history?limit=100`);
        const data = await res.json();

        if (data.success) {
            allHistoryData = data.data || [];
            // 默认显示全部
            applyFilter('all');
        } else {
            list.innerHTML = '<div style="text-align:center;padding:20px;color:gray;">暂无历史记录</div>';
        }
    } catch (e) {
        list.innerHTML = '加载失败';
    }
}

function applyFilter(status) {
    // 更新按钮状态
    document.querySelectorAll('.segment-btn').forEach(btn => {
        if (btn.textContent === '全部' && status === 'all') btn.classList.add('active');
        else if (btn.textContent === '成功' && status === 'success') btn.classList.add('active');
        else if (btn.textContent === '失败' && status === 'error') btn.classList.add('active');
        else btn.classList.remove('active');
    });

    // 筛选数据
    let filteredData = allHistoryData;
    if (status !== 'all') {
        filteredData = allHistoryData.filter(task => task.status === status);
    }

    renderHistoryList(filteredData);
}

function renderHistoryList(tasks) {
    const list = document.getElementById('history-list');

    if (tasks.length === 0) {
        list.innerHTML = '<div style="text-align:center;padding:20px;color:gray;">暂无相关记录</div>';
        return;
    }

    // 按日期分组
    const groups = {};
    tasks.forEach(task => {
        const date = new Date(task.created_at).toLocaleDateString();
        if (!groups[date]) groups[date] = [];
        groups[date].push(task);
    });

    // 生成 HTML
    list.innerHTML = Object.keys(groups).map((date, index) => {
        const groupTasks = groups[date];
        const count = groupTasks.length;
        const failedTasks = groupTasks.filter(t => t.status === 'error');
        const hasFailed = failedTasks.length > 0;
        // 默认全部折叠
        const isExpanded = '';

        return `
        <div class="history-group ${isExpanded}">
            <div class="history-group-header" onclick="toggleHistoryGroup(this)">
                <div class="history-group-info">
                    <span class="history-date">${date}</span>
                    <span class="history-count">${count} 条</span>
                </div>
                <div class="history-group-actions">
                    ${hasFailed ? `<button class="btn-text-sm error-retry-all" onclick='event.stopPropagation(); retryFailedTasksInGroup("${date}")'>重试失败 (${failedTasks.length})</button>` : ''}
                    <span class="history-toggle-icon">▼</span>
                </div>
            </div>
            <div class="history-group-content">
                ${groupTasks.map(task => `
                    <div class="history-item">
                        <div class="history-info">
                            <h4>${task.topic}</h4>
                            <div class="history-meta">
                                ${new Date(task.created_at).toLocaleTimeString()}
                                <span class="history-status ${task.status}">${task.status === 'error' ? '失败' : task.status}</span>
                            </div>
                        </div>
                        <div class="history-actions">
                            <!-- 无论任务状态如何，只要有生成的内容就显示查看按钮 -->
                            ${(task.title || task.content) ? `<button class="btn-text" onclick='showResultModal(${JSON.stringify(task).replace(/'/g, "&#39;")})'>查看</button>` : ''}
                            ${task.status === 'error' ? `<button class="btn-text error-retry" onclick='retryTask(${JSON.stringify(task).replace(/'/g, "&#39;")})'>重试</button>` : ''}
                            <button class="btn-icon-sm delete-btn" onclick='deleteTask("${task.task_id}")' title="删除">🗑️</button>
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
    `}).join('');
}

async function deleteTask(taskId) {
    if (!confirm('确定要删除这条记录吗？')) return;

    try {
        const res = await fetch(`${API_BASE}/history/${taskId}`, { method: 'DELETE' });
        const data = await res.json();
        if (data.success) {
            showToast('删除成功', 'success');
            loadTaskHistory(); // Reload list
        } else {
            showToast(data.detail || '删除失败', 'error');
        }
    } catch (e) {
        showToast('删除出错', 'error');
    }
}

async function retryFailedTasksInGroup(date) {
    // Find tasks for this date
    // Note: allHistoryData is flattened, we need to filter by date string
    const tasksToRetry = allHistoryData.filter(task => {
        const taskDate = new Date(task.created_at).toLocaleDateString();
        return taskDate === date && task.status === 'error';
    });

    if (tasksToRetry.length === 0) return;

    if (!confirm(`确定要重试该日期的 ${tasksToRetry.length} 个失败任务吗？`)) return;

    closeModal();
    switchView('home');
    showToast(`开始批量重试 ${tasksToRetry.length} 个任务`, 'success');

    for (const task of tasksToRetry) {
        await new Promise(r => setTimeout(r, 300));
        const contentType = task.content_type || null;
        startGenerate(task.topic, contentType, task.id); // 传递 task.id
    }
}

function retryTask(task) {
    closeModal();
    switchView('home');

    // 自动填充并开始生成
    // 如果历史记录里存了 content_type 就用，没有就默认 null (使用当前选中)
    const contentType = task.content_type || null;
    startGenerate(task.topic, contentType, task.id); // 传递 task.id
}

function toggleHistoryGroup(header) {
    const group = header.parentElement;
    group.classList.toggle('expanded');
}

// --- 工具函数 ---
function showToast(msg, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = msg;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
