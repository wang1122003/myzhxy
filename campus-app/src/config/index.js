/**
 * 全局配置文件
 */

const config = {
  // API基础URL配置
  baseApi: {
    // 开发环境
    dev: 'http://localhost:8080/api',
    // 生产环境
    prod: '/api'
  },
  
  // 上传文件配置
  upload: {
    // 上传文件大小限制（单位：MB）
    maxSize: 5,
    // 允许上传的文件类型
    allowedTypes: ['.jpg', '.jpeg', '.png', '.gif', '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx']
  },
  
  // 分页配置
  pagination: {
    // 默认每页显示条数
    pageSize: 10,
    // 每页条数选项
    pageSizes: [10, 20, 50, 100]
  },
  
  // 系统配置
  system: {
    // 系统名称
    name: '智慧校园管理系统',
    // 系统版本
    version: '1.0.0',
    // 系统Logo
    logo: '/logo.png',
    // 登录页背景图
    loginBg: '/login-bg.jpg'
  },
  
  // 请求超时时间（单位：毫秒）
  timeout: 15000,
  
  // TOKEN相关
  token: {
    // 存储在localStorage中的键名
    name: 'campus_token',
    // 过期时间（单位：小时）
    expireTime: 24
  }
}

export default config 