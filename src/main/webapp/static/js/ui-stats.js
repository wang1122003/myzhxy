/**
 * UI统计模块 - 负责数据统计和图表展示
 */
(function(window) {
    'use strict';
    
    // 确保UI对象存在
    window.UI = window.UI || {};
    
    // 创建UIStats对象
    var UIStats = {
        // 初始化统计面板
        initStatsPanel: function(container, options) {
            var defaultOptions = {
                refreshInterval: 30000, // 数据刷新间隔，默认30秒
                showFilters: true,      // 是否显示筛选器
                chartType: 'bar',       // 默认图表类型
                dateRange: 'week'       // 默认日期范围
            };
            
            options = Object.assign({}, defaultOptions, options || {});
            this.container = typeof container === 'string' ? document.querySelector(container) : container;
            this.options = options;
            
            if (!this.container) {
                console.error('统计面板容器未找到');
                return;
            }
            
            this._createPanelStructure();
            this.bindStatsEvents();
            this.loadInitialData();
            
            // 设置定时刷新
            if (options.refreshInterval > 0) {
                this.refreshTimer = setInterval(function() {
                    this.refreshData();
                }.bind(this), options.refreshInterval);
            }
        },
        
        // 创建统计面板基本结构
        _createPanelStructure: function() {
            var panelHtml = '<div class="stats-container">' +
                '<div class="stats-header">' +
                    '<h3>数据统计</h3>' +
                    '<div class="stats-controls">';
            
            if (this.options.showFilters) {
                panelHtml += '<div class="stats-filters">' +
                    '<select class="date-range-selector">' +
                        '<option value="day">今日</option>' +
                        '<option value="week" selected>本周</option>' +
                        '<option value="month">本月</option>' +
                        '<option value="year">本年</option>' +
                        '<option value="custom">自定义</option>' +
                    '</select>' +
                    '<select class="chart-type-selector">' +
                        '<option value="bar">柱状图</option>' +
                        '<option value="line">折线图</option>' +
                        '<option value="pie">饼图</option>' +
                    '</select>' +
                    '<button class="refresh-btn">刷新</button>' +
                '</div>';
            }
            
            panelHtml += '</div></div>' +
                '<div class="stats-content">' +
                    '<div class="stats-summary">' +
                        '<div class="stat-card total-users"><h4>用户总数</h4><div class="stat-value">--</div></div>' +
                        '<div class="stat-card total-courses"><h4>课程总数</h4><div class="stat-value">--</div></div>' +
                        '<div class="stat-card total-activities"><h4>活动总数</h4><div class="stat-value">--</div></div>' +
                        '<div class="stat-card total-posts"><h4>文章总数</h4><div class="stat-value">--</div></div>' +
                    '</div>' +
                    '<div class="stats-charts">' +
                        '<div class="chart-container primary-chart"><canvas id="primaryChart"></canvas></div>' +
                        '<div class="chart-container secondary-charts">' +
                            '<div class="secondary-chart"><canvas id="secondaryChart1"></canvas></div>' +
                            '<div class="secondary-chart"><canvas id="secondaryChart2"></canvas></div>' +
                        '</div>' +
                    '</div>' +
                '</div>' +
                '<div class="stats-loading">加载中...</div>' +
            '</div>';
            
            this.container.innerHTML = panelHtml;
            this.loadingEl = this.container.querySelector('.stats-loading');
            
            // 如果需要自定义日期范围，添加日期选择器
            var dateRangeSelector = this.container.querySelector('.date-range-selector');
            if (dateRangeSelector) {
                dateRangeSelector.addEventListener('change', function(e) {
                    if (e.target.value === 'custom') {
                        this._showDateRangePicker();
                    } else {
                        this.options.dateRange = e.target.value;
                        this.loadData();
                    }
                }.bind(this));
            }
        },
        
        // 绑定统计面板事件
        bindStatsEvents: function() {
            var refreshBtn = this.container.querySelector('.refresh-btn');
            if (refreshBtn) {
                refreshBtn.addEventListener('click', this.refreshData.bind(this));
            }
            
            var chartTypeSelector = this.container.querySelector('.chart-type-selector');
            if (chartTypeSelector) {
                chartTypeSelector.addEventListener('change', function(e) {
                    this.options.chartType = e.target.value;
                    this.updateCharts();
                }.bind(this));
            }
        },
        
        // 显示日期范围选择器
        _showDateRangePicker: function() {
            var datePickerHtml = '<div class="date-range-picker">' +
                '<div class="date-inputs">' +
                    '<label>开始日期: <input type="date" class="start-date"></label>' +
                    '<label>结束日期: <input type="date" class="end-date"></label>' +
                '</div>' +
                '<div class="date-actions">' +
                    '<button class="apply-dates">应用</button>' +
                    '<button class="cancel-dates">取消</button>' +
                '</div>' +
            '</div>';
            
            var datePickerContainer = document.createElement('div');
            datePickerContainer.className = 'date-picker-overlay';
            datePickerContainer.innerHTML = datePickerHtml;
            document.body.appendChild(datePickerContainer);
            
            // 设置默认日期（今天和7天前）
            var today = new Date();
            var weekAgo = new Date();
            weekAgo.setDate(weekAgo.getDate() - 7);
            
            var startDateInput = datePickerContainer.querySelector('.start-date');
            var endDateInput = datePickerContainer.querySelector('.end-date');
            
            startDateInput.valueAsDate = weekAgo;
            endDateInput.valueAsDate = today;
            
            // 绑定事件
            datePickerContainer.querySelector('.apply-dates').addEventListener('click', function() {
                this.customDateRange = {
                    start: startDateInput.value,
                    end: endDateInput.value
                };
                document.body.removeChild(datePickerContainer);
                this.loadData();
            }.bind(this));
            
            datePickerContainer.querySelector('.cancel-dates').addEventListener('click', function() {
                document.body.removeChild(datePickerContainer);
                // 重置选择器为之前的值
                var dateRangeSelector = this.container.querySelector('.date-range-selector');
                dateRangeSelector.value = this.options.dateRange;
            }.bind(this));
        },
        
        // 加载初始数据
        loadInitialData: function() {
            this.showLoading();
            this.loadSummaryData();
            this.loadData();
        },
        
        // 刷新数据
        refreshData: function() {
            this.showLoading();
            this.loadSummaryData();
            this.loadData();
        },
        
        // 加载汇总数据
        loadSummaryData: function() {
            // 请求汇总数据
            UI.request({
                url: '/api/stats/summary',
                method: 'GET',
                success: function(data) {
                    this.updateSummaryCards(data);
                }.bind(this),
                error: function(error) {
                    UI.showMessage('加载统计摘要失败: ' + error.message, 'error');
                    this.hideLoading();
                }.bind(this)
            });
        },
        
        // 更新汇总卡片
        updateSummaryCards: function(data) {
            var cards = {
                totalUsers: this.container.querySelector('.total-users .stat-value'),
                totalCourses: this.container.querySelector('.total-courses .stat-value'),
                totalActivities: this.container.querySelector('.total-activities .stat-value'),
                totalPosts: this.container.querySelector('.total-posts .stat-value')
            };
            
            if (cards.totalUsers) cards.totalUsers.textContent = data.userCount || 0;
            if (cards.totalCourses) cards.totalCourses.textContent = data.courseCount || 0;
            if (cards.totalActivities) cards.totalActivities.textContent = data.activityCount || 0;
            if (cards.totalPosts) cards.totalPosts.textContent = data.postCount || 0;
        },
        
        // 加载统计数据
        loadData: function() {
            var params = {
                dateRange: this.options.dateRange
            };
            
            // 如果是自定义日期范围
            if (this.options.dateRange === 'custom' && this.customDateRange) {
                params.startDate = this.customDateRange.start;
                params.endDate = this.customDateRange.end;
            }
            
            // 请求图表数据
            UI.request({
                url: '/api/stats/charts',
                method: 'GET',
                data: params,
                success: function(data) {
                    this.chartsData = data;
                    this.renderCharts();
                    this.hideLoading();
                }.bind(this),
                error: function(error) {
                    UI.showMessage('加载统计数据失败: ' + error.message, 'error');
                    this.hideLoading();
                }.bind(this)
            });
        },
        
        // 渲染图表
        renderCharts: function() {
            if (!this.chartsData) return;
            
            // 清除现有图表
            this.destroyCharts();
            
            // 创建主图表
            this.createPrimaryChart();
            
            // 创建次要图表
            this.createSecondaryCharts();
        },
        
        // 创建主图表
        createPrimaryChart: function() {
            var ctx = document.getElementById('primaryChart').getContext('2d');
            var data = this.chartsData.userActivity;
            
            this.primaryChart = new Chart(ctx, {
                type: this.options.chartType,
                data: {
                    labels: data.labels,
                    datasets: [{
                        label: '用户活跃度',
                        data: data.values,
                        backgroundColor: 'rgba(75, 192, 192, 0.2)',
                        borderColor: 'rgba(75, 192, 192, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        },
        
        // 创建次要图表
        createSecondaryCharts: function() {
            // 课程参与度图表
            var ctx1 = document.getElementById('secondaryChart1').getContext('2d');
            var courseData = this.chartsData.courseEngagement;
            
            this.secondaryChart1 = new Chart(ctx1, {
                type: 'pie',
                data: {
                    labels: courseData.labels,
                    datasets: [{
                        label: '课程参与度',
                        data: courseData.values,
                        backgroundColor: [
                            'rgba(255, 99, 132, 0.2)',
                            'rgba(54, 162, 235, 0.2)',
                            'rgba(255, 206, 86, 0.2)',
                            'rgba(75, 192, 192, 0.2)',
                            'rgba(153, 102, 255, 0.2)'
                        ],
                        borderColor: [
                            'rgba(255, 99, 132, 1)',
                            'rgba(54, 162, 235, 1)',
                            'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)',
                            'rgba(153, 102, 255, 1)'
                        ],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false
                }
            });
            
            // 论坛活跃度图表
            var ctx2 = document.getElementById('secondaryChart2').getContext('2d');
            var forumData = this.chartsData.forumActivity;
            
            this.secondaryChart2 = new Chart(ctx2, {
                type: 'bar',
                data: {
                    labels: forumData.labels,
                    datasets: [{
                        label: '论坛活跃度',
                        data: forumData.values,
                        backgroundColor: 'rgba(153, 102, 255, 0.2)',
                        borderColor: 'rgba(153, 102, 255, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        },
        
        // 更新图表
        updateCharts: function() {
            // 销毁现有图表
            this.destroyCharts();
            
            // 重新创建图表
            this.renderCharts();
        },
        
        // 销毁图表
        destroyCharts: function() {
            if (this.primaryChart) {
                this.primaryChart.destroy();
                this.primaryChart = null;
            }
            
            if (this.secondaryChart1) {
                this.secondaryChart1.destroy();
                this.secondaryChart1 = null;
            }
            
            if (this.secondaryChart2) {
                this.secondaryChart2.destroy();
                this.secondaryChart2 = null;
            }
        },
        
        // 显示加载状态
        showLoading: function() {
            if (this.loadingEl) {
                this.loadingEl.style.display = 'flex';
            }
        },
        
        // 隐藏加载状态
        hideLoading: function() {
            if (this.loadingEl) {
                this.loadingEl.style.display = 'none';
            }
        },
        
        // 导出统计数据
        exportData: function(format) {
            format = format || 'csv';
            var dateRange = this.options.dateRange;
            var customRange = '';
            
            if (dateRange === 'custom' && this.customDateRange) {
                customRange = '&startDate=' + this.customDateRange.start + '&endDate=' + this.customDateRange.end;
            }
            
            window.location.href = '/api/stats/export?format=' + format + '&dateRange=' + dateRange + customRange;
        },
        
        // 打印统计报表
        printStats: function() {
            var printWindow = window.open('', '_blank');
            var chartImages = [];
            var chartsToCapture = ['primaryChart', 'secondaryChart1', 'secondaryChart2'];
            
            // 捕获所有图表为图片
            Promise.all(chartsToCapture.map(function(id) {
                return new Promise(function(resolve) {
                    var canvas = document.getElementById(id);
                    if (canvas) {
                        chartImages.push({
                            id: id,
                            image: canvas.toDataURL('image/png')
                        });
                    }
                    resolve();
                });
            })).then(function() {
                // 构建打印页面
                var summaryEl = this.container.querySelector('.stats-summary');
                var dateRangeText = this._getDateRangeText();
                
                var printContent = '<html><head><title>统计报表</title>';
                printContent += '<style>body{font-family:Arial,sans-serif;padding:20px;}';
                printContent += '.report-header{text-align:center;margin-bottom:20px;}';
                printContent += '.summary-cards{display:flex;justify-content:space-between;margin-bottom:30px;}';
                printContent += '.summary-card{border:1px solid #ddd;padding:15px;border-radius:5px;flex:1;margin:0 10px;text-align:center;}';
                printContent += '.chart-container{margin-bottom:30px;text-align:center;}';
                printContent += 'h2,h3{color:#333;}';
                printContent += '.chart-image{max-width:100%;height:auto;}';
                printContent += '</style></head><body>';
                
                // 报表头部
                printContent += '<div class="report-header">';
                printContent += '<h1>数据统计报表</h1>';
                printContent += '<p>日期范围: ' + dateRangeText + '</p>';
                printContent += '<p>生成时间: ' + new Date().toLocaleString() + '</p>';
                printContent += '</div>';
                
                // 统计摘要
                printContent += '<h2>数据摘要</h2>';
                printContent += '<div class="summary-cards">';
                
                if (summaryEl) {
                    var cards = summaryEl.querySelectorAll('.stat-card');
                    cards.forEach(function(card) {
                        var title = card.querySelector('h4').textContent;
                        var value = card.querySelector('.stat-value').textContent;
                        printContent += '<div class="summary-card">';
                        printContent += '<h3>' + title + '</h3>';
                        printContent += '<div class="value">' + value + '</div>';
                        printContent += '</div>';
                    });
                }
                
                printContent += '</div>';
                
                // 图表
                printContent += '<h2>数据图表</h2>';
                
                chartImages.forEach(function(chart) {
                    var title = '';
                    switch(chart.id) {
                        case 'primaryChart':
                            title = '用户活跃度趋势';
                            break;
                        case 'secondaryChart1':
                            title = '课程参与度分布';
                            break;
                        case 'secondaryChart2':
                            title = '论坛活跃度统计';
                            break;
                    }
                    
                    printContent += '<div class="chart-container">';
                    printContent += '<h3>' + title + '</h3>';
                    printContent += '<img class="chart-image" src="' + chart.image + '" alt="' + title + '">';
                    printContent += '</div>';
                });
                
                printContent += '</body></html>';
                
                printWindow.document.open();
                printWindow.document.write(printContent);
                printWindow.document.close();
                
                setTimeout(function() {
                    printWindow.print();
                }, 500);
            }.bind(this));
        },
        
        // 获取日期范围文本描述
        _getDateRangeText: function() {
            var text = '';
            switch(this.options.dateRange) {
                case 'day':
                    text = '今日';
                    break;
                case 'week':
                    text = '本周';
                    break;
                case 'month':
                    text = '本月';
                    break;
                case 'year':
                    text = '本年';
                    break;
                case 'custom':
                    if (this.customDateRange) {
                        text = this.customDateRange.start + ' 至 ' + this.customDateRange.end;
                    } else {
                        text = '自定义';
                    }
                    break;
                default:
                    text = this.options.dateRange;
            }
            return text;
        }
    };
    
    // 将UIStats对象添加到全局UI对象
    window.UI.Stats = UIStats;
    
})(window); 