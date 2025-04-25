// Basic form validation rules
const formRules = {
    required: (field = '该字段') => ({
        required: true,
        message: `${field}不能为空`,
        trigger: 'blur' // Or 'change' depending on the component
    }),
    // Add other common rules like email, phone, length etc. as needed
    // Example email rule:
    // email: {
    //   type: 'email',
    //   message: '请输入有效的邮箱地址',
    //   trigger: ['blur', 'change']
    // }
};

export default formRules; 