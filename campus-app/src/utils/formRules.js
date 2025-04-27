// 通用表单验证规则
const formRules = {
    required: (field = '该字段') => ({
        required: true,
        message: `${field}不能为空`,
        trigger: 'blur' // 或根据组件类型使用 'change'
    }),
    // 可根据需要添加其他常用规则，如 email, phone, length 等
    // 示例 email 规则:
    // email: {
    //   type: 'email',
    //   message: '请输入有效的邮箱地址',
    //   trigger: ['blur', 'change']
    // }
};

export default formRules; 