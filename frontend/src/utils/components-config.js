// 公共组件配置

export const tableConfig = {
    pageSize: 10,
    rowKey: 'id',
    paginationPosition: 'bottom'
};

export const formRules = {
    required: value => !!value || '必填项',
    email: value => /.+@.+\..+/.test(value) || '邮箱格式错误',
    phone: value => /^1[3-9]\d{9}$/.test(value) || '手机号格式错误'
};