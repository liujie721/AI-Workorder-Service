// 公共API请求封装，自动处理基础URL和错误
const API_BASE = window.location.origin || 'http://localhost:8080';

async function apiRequest(endpoint, method = 'GET', body = null, customHeaders = {}) {
    const url = `${API_BASE}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json',
        ...customHeaders
    };
    const config = {
        method,
        headers,
    };
    if (body && (method === 'POST' || method === 'PUT')) {
        config.body = JSON.stringify(body);
    }
    const response = await fetch(url, config);
    let data;
    try {
        data = await response.json();
    } catch (e) {
        throw new Error(`请求失败: ${response.status}`);
    }
    if (data.code !== 200 && data.code !== 200) { // 统一成功状态码200
        throw new Error(data.message || `请求异常 (${data.code})`);
    }
    return data;
}