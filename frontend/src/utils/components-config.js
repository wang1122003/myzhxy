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

export const WEEKDAY_OPTIONS = [
    {label: '星期一', value: 1},
    {label: '星期二', value: 2},
    {label: '星期三', value: 3},
    {label: '星期四', value: 4},
    {label: '星期五', value: 5},
    {label: '星期六', value: 6},
    {label: '星期日', value: 7},
];