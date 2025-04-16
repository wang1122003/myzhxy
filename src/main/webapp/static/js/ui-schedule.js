/**
 * 课表模块UI组件
 * 提供课表相关的UI功能
 */
const UISchedule = {
    /**
     * 初始化课表
     * @param {string} containerId 容器ID
     * @param {Object} options 选项
     */
    initSchedule: function(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 默认选项
        const defaultOptions = {
            weekCount: 20,
            startHour: 8,
            endHour: 22,
            periodCount: 12,
            viewType: 'week',
            currentWeek: 1,
            showWeekend: true
        };
        
        // 合并选项
        const mergedOptions = { ...defaultOptions, ...options };
        this.options = mergedOptions;
        
        // 创建课表头部
        const header = this.createScheduleHeader(mergedOptions);
        
        // 创建课表主体
        const body = this.createScheduleBody(mergedOptions);
        
        // 组合
        container.innerHTML = '';
        container.appendChild(header);
        container.appendChild(body);
        
        // 加载课程数据
        this.loadScheduleData(containerId);
        
        // 绑定事件
        this.bindScheduleEvents(container);
    },
    
    /**
     * 创建课表头部
     * @param {Object} options 选项
     * @returns {HTMLElement} 头部元素
     */
    createScheduleHeader: function(options) {
        const header = document.createElement('div');
        header.className = 'schedule-header';
        
        // 创建切换按钮
        const switchButtons = document.createElement('div');
        switchButtons.className = 'schedule-switch';
        
        // 上一周/月按钮
        const prevBtn = document.createElement('button');
        prevBtn.className = 'btn btn-outline-primary prev-btn';
        prevBtn.innerHTML = '<i class="bi bi-chevron-left"></i>';
        prevBtn.setAttribute('data-action', 'prev');
        
        // 下一周/月按钮
        const nextBtn = document.createElement('button');
        nextBtn.className = 'btn btn-outline-primary next-btn';
        nextBtn.innerHTML = '<i class="bi bi-chevron-right"></i>';
        nextBtn.setAttribute('data-action', 'next');
        
        // 今天按钮
        const todayBtn = document.createElement('button');
        todayBtn.className = 'btn btn-primary today-btn';
        todayBtn.textContent = '今天';
        todayBtn.setAttribute('data-action', 'today');
        
        switchButtons.appendChild(prevBtn);
        switchButtons.appendChild(todayBtn);
        switchButtons.appendChild(nextBtn);
        
        // 创建标题
        const title = document.createElement('div');
        title.className = 'schedule-title';
        
        // 当前周/月信息
        const currentInfo = document.createElement('h3');
        currentInfo.className = 'current-info';
        currentInfo.textContent = `第${options.currentWeek}周`;
        
        // 日期范围
        const dateRange = document.createElement('div');
        dateRange.className = 'date-range';
        
        // 计算当前周的日期范围
        const today = new Date();
        const firstDayOfSemester = this.getFirstDayOfSemester(); // 学期第一天
        
        // 计算当前周的第一天
        const firstDayOfWeek = new Date(firstDayOfSemester);
        firstDayOfWeek.setDate(firstDayOfSemester.getDate() + (options.currentWeek - 1) * 7);
        
        // 计算当前周的最后一天
        const lastDayOfWeek = new Date(firstDayOfWeek);
        lastDayOfWeek.setDate(firstDayOfWeek.getDate() + 6);
        
        // 格式化日期
        const formatDate = (date) => {
            const year = date.getFullYear();
            const month = date.getMonth() + 1;
            const day = date.getDate();
            return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
        };
        
        dateRange.textContent = `${formatDate(firstDayOfWeek)} ~ ${formatDate(lastDayOfWeek)}`;
        
        title.appendChild(currentInfo);
        title.appendChild(dateRange);
        
        // 视图切换
        const viewSwitch = document.createElement('div');
        viewSwitch.className = 'view-switch';
        
        // 周视图按钮
        const weekViewBtn = document.createElement('button');
        weekViewBtn.className = `btn ${options.viewType === 'week' ? 'btn-primary' : 'btn-outline-primary'}`;
        weekViewBtn.textContent = '周视图';
        weekViewBtn.setAttribute('data-view', 'week');
        
        // 月视图按钮
        const monthViewBtn = document.createElement('button');
        monthViewBtn.className = `btn ${options.viewType === 'month' ? 'btn-primary' : 'btn-outline-primary'}`;
        monthViewBtn.textContent = '月视图';
        monthViewBtn.setAttribute('data-view', 'month');
        
        viewSwitch.appendChild(weekViewBtn);
        viewSwitch.appendChild(monthViewBtn);
        
        // 添加到头部
        header.appendChild(switchButtons);
        header.appendChild(title);
        header.appendChild(viewSwitch);
        
        return header;
    },
    
    /**
     * 创建课表主体
     * @param {Object} options 选项
     * @returns {HTMLElement} 主体元素
     */
    createScheduleBody: function(options) {
        if (options.viewType === 'week') {
            return this.createWeekView(options);
        } else {
            return this.createMonthView(options);
        }
    },
    
    /**
     * 创建周视图
     * @param {Object} options 选项
     * @returns {HTMLElement} 周视图元素
     */
    createWeekView: function(options) {
        const weekView = document.createElement('div');
        weekView.className = 'schedule-week-view';
        
        // 创建表格
        const table = document.createElement('table');
        table.className = 'schedule-table';
        
        // 创建表头
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        
        // 时间列
        const timeHeader = document.createElement('th');
        timeHeader.className = 'time-header';
        timeHeader.textContent = '时间/日期';
        headerRow.appendChild(timeHeader);
        
        // 星期列
        const days = ['一', '二', '三', '四', '五'];
        if (options.showWeekend) {
            days.push('六', '日');
        }
        
        // 计算当前周的第一天
        const firstDayOfSemester = this.getFirstDayOfSemester(); // 学期第一天
        const firstDayOfWeek = new Date(firstDayOfSemester);
        firstDayOfWeek.setDate(firstDayOfSemester.getDate() + (options.currentWeek - 1) * 7);
        
        days.forEach((day, index) => {
            const dayHeader = document.createElement('th');
            dayHeader.className = 'day-header';
            
            // 计算当天日期
            const date = new Date(firstDayOfWeek);
            date.setDate(firstDayOfWeek.getDate() + index);
            
            // 格式化日期
            const month = date.getMonth() + 1;
            const dayOfMonth = date.getDate();
            
            // 检查是否是今天
            const today = new Date();
            const isToday = date.getDate() === today.getDate() && 
                           date.getMonth() === today.getMonth() && 
                           date.getFullYear() === today.getFullYear();
            
            // 添加日期信息
            dayHeader.innerHTML = `
                <div class="day-name">周${day}</div>
                <div class="day-date ${isToday ? 'today' : ''}">${month}/${dayOfMonth}</div>
            `;
            
            headerRow.appendChild(dayHeader);
        });
        
        thead.appendChild(headerRow);
        table.appendChild(thead);
        
        // 创建表体
        const tbody = document.createElement('tbody');
        
        // 创建课程时间段
        for (let i = 0; i < options.periodCount; i++) {
            const row = document.createElement('tr');
            row.className = `period-row period-${i + 1}`;
            
            // 时间单元格
            const timeCell = document.createElement('td');
            timeCell.className = 'time-cell';
            
            // 计算时间
            const startHour = options.startHour + Math.floor(i / 2);
            const startMinute = (i % 2) === 0 ? '00' : '30';
            const endHour = options.startHour + Math.floor((i + 1) / 2);
            const endMinute = ((i + 1) % 2) === 0 ? '00' : '30';
            
            timeCell.innerHTML = `
                <div class="period-number">${i + 1}</div>
                <div class="period-time">${startHour}:${startMinute}-${endHour}:${endMinute}</div>
            `;
            
            row.appendChild(timeCell);
            
            // 创建每天的单元格
            for (let j = 0; j < days.length; j++) {
                const dayCell = document.createElement('td');
                dayCell.className = 'day-cell';
                dayCell.setAttribute('data-day', j);
                dayCell.setAttribute('data-period', i);
                
                // 添加空白课程占位
                dayCell.innerHTML = `<div class="course-placeholder"></div>`;
                
                row.appendChild(dayCell);
            }
            
            tbody.appendChild(row);
        }
        
        table.appendChild(tbody);
        weekView.appendChild(table);
        
        return weekView;
    },
    
    /**
     * 创建月视图
     * @param {Object} options 选项
     * @returns {HTMLElement} 月视图元素
     */
    createMonthView: function(options) {
        const monthView = document.createElement('div');
        monthView.className = 'schedule-month-view';
        
        // 计算当前月的第一天
        const today = new Date();
        const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
        
        // 计算当前月的天数
        const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
        const daysInMonth = lastDayOfMonth.getDate();
        
        // 计算第一天是星期几
        const firstDayWeekday = firstDayOfMonth.getDay(); // 0是星期日
        
        // 创建日历表格
        const table = document.createElement('table');
        table.className = 'calendar-table';
        
        // 创建表头
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        
        // 星期头
        const weekdays = ['日', '一', '二', '三', '四', '五', '六'];
        weekdays.forEach(day => {
            const th = document.createElement('th');
            th.textContent = `周${day}`;
            headerRow.appendChild(th);
        });
        
        thead.appendChild(headerRow);
        table.appendChild(thead);
        
        // 创建表体
        const tbody = document.createElement('tbody');
        
        // 计算日历格子总数（最多6行）
        let cells = [];
        
        // 上个月的剩余天数
        const prevMonthDays = new Date(today.getFullYear(), today.getMonth(), 0).getDate();
        for (let i = 0; i < firstDayWeekday; i++) {
            cells.push({
                day: prevMonthDays - firstDayWeekday + i + 1,
                isCurrentMonth: false,
                isToday: false
            });
        }
        
        // 当前月的天数
        for (let i = 1; i <= daysInMonth; i++) {
            cells.push({
                day: i,
                isCurrentMonth: true,
                isToday: i === today.getDate()
            });
        }
        
        // 下个月的开始天数（补满6行*7列=42个格子）
        const remainingCells = 42 - cells.length;
        for (let i = 1; i <= remainingCells; i++) {
            cells.push({
                day: i,
                isCurrentMonth: false,
                isToday: false
            });
        }
        
        // 创建行和单元格
        for (let i = 0; i < 6; i++) {
            const row = document.createElement('tr');
            
            for (let j = 0; j < 7; j++) {
                const index = i * 7 + j;
                const cell = cells[index];
                
                const td = document.createElement('td');
                td.className = `calendar-cell ${cell.isCurrentMonth ? 'current-month' : 'other-month'} ${cell.isToday ? 'today' : ''}`;
                
                // 日期
                const dateDiv = document.createElement('div');
                dateDiv.className = 'date-number';
                dateDiv.textContent = cell.day;
                
                // 课程容器
                const coursesDiv = document.createElement('div');
                coursesDiv.className = 'day-courses';
                
                td.appendChild(dateDiv);
                td.appendChild(coursesDiv);
                
                // 设置属性
                td.setAttribute('data-date', cell.day);
                td.setAttribute('data-current-month', cell.isCurrentMonth ? '1' : '0');
                
                row.appendChild(td);
            }
            
            tbody.appendChild(row);
        }
        
        table.appendChild(tbody);
        monthView.appendChild(table);
        
        return monthView;
    },
    
    /**
     * 获取学期第一天
     * @returns {Date} 学期第一天
     */
    getFirstDayOfSemester: function() {
        // 这里可以通过API获取，这里简单示例
        const currentYear = new Date().getFullYear();
        const currentMonth = new Date().getMonth();
        
        // 假设春季学期从2月开始，秋季学期从9月开始
        if (currentMonth < 7) { // 春季学期
            return new Date(currentYear, 1, 1); // 2月1日
        } else { // 秋季学期
            return new Date(currentYear, 8, 1); // 9月1日
        }
    },
    
    /**
     * 加载课表数据
     * @param {string} containerId 容器ID
     */
    loadScheduleData: function(containerId) {
        UI.showLoading();
        
        // 确定API调用参数
        let params = {};
        
        if (this.options.viewType === 'week') {
            params.week = this.options.currentWeek;
        } else {
            // 获取当前月份
            const today = new Date();
            params.year = today.getFullYear();
            params.month = today.getMonth() + 1;
        }
        
        API.schedule.getCourseSchedule(params)
            .then(response => {
                if (response.code === 200) {
                    if (this.options.viewType === 'week') {
                        this.renderWeekCourses(containerId, response.data);
                    } else {
                        this.renderMonthCourses(containerId, response.data);
                    }
                } else {
                    UI.showMessage(response.message || '获取课表失败', 'error');
                }
            })
            .catch(error => {
                console.error('获取课表失败:', error);
                UI.showMessage('获取课表失败', 'error');
            })
            .finally(() => {
                UI.hideLoading();
            });
    },
    
    /**
     * 渲染周视图课程
     * @param {string} containerId 容器ID
     * @param {Array} courses 课程数据
     */
    renderWeekCourses: function(containerId, courses) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 清除已有课程
        const existingCourses = container.querySelectorAll('.course-item');
        existingCourses.forEach(course => {
            course.remove();
        });
        
        // 渲染课程
        courses.forEach(course => {
            // 计算位置
            const day = course.day - 1; // 0-6 对应周一到周日
            const startPeriod = course.startPeriod - 1; // 0开始的课程节次
            const endPeriod = course.endPeriod - 1; // 0开始的课程节次
            const span = endPeriod - startPeriod + 1; // 课程跨度
            
            // 检查是否在显示范围内
            if (
                (day >= 5 && !this.options.showWeekend) || // 周末但不显示周末
                day < 0 || day > 6 // 无效日期
            ) {
                return;
            }
            
            // 获取对应单元格
            const cell = container.querySelector(`.day-cell[data-day="${day}"][data-period="${startPeriod}"]`);
            if (!cell) return;
            
            // 创建课程元素
            const courseEl = document.createElement('div');
            courseEl.className = `course-item ${this.getCourseColorClass(course.courseId)}`;
            courseEl.style.height = `calc(${span} * 100%)`;
            courseEl.setAttribute('data-course-id', course.courseId);
            
            // 课程信息
            courseEl.innerHTML = `
                <div class="course-name">${course.courseName}</div>
                <div class="course-info">
                    <div class="course-teacher">${course.teacherName}</div>
                    <div class="course-location">${course.location}</div>
                </div>
            `;
            
            // 添加点击事件
            courseEl.addEventListener('click', () => {
                this.showCourseDetail(course);
            });
            
            // 替换占位符
            cell.innerHTML = '';
            cell.appendChild(courseEl);
        });
    },
    
    /**
     * 渲染月视图课程
     * @param {string} containerId 容器ID
     * @param {Array} courses 课程数据
     */
    renderMonthCourses: function(containerId, courses) {
        const container = document.getElementById(containerId);
        if (!container) return;
        
        // 获取所有当前月的单元格
        const currentMonthCells = container.querySelectorAll('.calendar-cell.current-month');
        
        // 清除已有课程
        currentMonthCells.forEach(cell => {
            const coursesContainer = cell.querySelector('.day-courses');
            if (coursesContainer) {
                coursesContainer.innerHTML = '';
            }
        });
        
        // 按日期分组课程
        const coursesByDate = {};
        courses.forEach(course => {
            // 从日期中提取日
            const day = new Date(course.date).getDate();
            
            if (!coursesByDate[day]) {
                coursesByDate[day] = [];
            }
            coursesByDate[day].push(course);
        });
        
        // 渲染课程
        currentMonthCells.forEach(cell => {
            const day = parseInt(cell.getAttribute('data-date'));
            const coursesForDay = coursesByDate[day] || [];
            
            // 获取课程容器
            const coursesContainer = cell.querySelector('.day-courses');
            if (!coursesContainer) return;
            
            // 渲染课程
            coursesForDay.forEach(course => {
                const courseEl = document.createElement('div');
                courseEl.className = `month-course-item ${this.getCourseColorClass(course.courseId)}`;
                courseEl.setAttribute('data-course-id', course.courseId);
                courseEl.innerHTML = `
                    <span class="course-time">${course.startPeriod}-${course.endPeriod}节</span>
                    <span class="course-name">${course.courseName}</span>
                `;
                
                // 添加点击事件
                courseEl.addEventListener('click', () => {
                    this.showCourseDetail(course);
                });
                
                coursesContainer.appendChild(courseEl);
            });
        });
    },
    
    /**
     * 获取课程颜色类
     * @param {string} courseId 课程ID
     * @returns {string} 颜色类名
     */
    getCourseColorClass: function(courseId) {
        // 根据课程ID生成一个0-9的数字
        const colorIndex = parseInt(courseId.toString().split('').reduce((acc, char) => acc + char.charCodeAt(0), 0)) % 10;
        return `course-color-${colorIndex}`;
    },
    
    /**
     * 显示课程详情
     * @param {Object} course 课程数据
     */
    showCourseDetail: function(course) {
        // 创建详情内容
        let content = `
            <div class="course-detail">
                <h3 class="course-name">${course.courseName}</h3>
                
                <div class="detail-section">
                    <div class="detail-item">
                        <label>教师：</label>
                        <span>${course.teacherName}</span>
                    </div>
                    <div class="detail-item">
                        <label>地点：</label>
                        <span>${course.location}</span>
                    </div>
                    <div class="detail-item">
                        <label>时间：</label>
                        <span>周${['一', '二', '三', '四', '五', '六', '日'][course.day - 1]} 第${course.startPeriod}-${course.endPeriod}节</span>
                    </div>
                    <div class="detail-item">
                        <label>周次：</label>
                        <span>${this.formatWeekRange(course.weeks)}</span>
                    </div>
                </div>
                
                <div class="detail-section">
                    <h4>课程简介</h4>
                    <p>${course.description || '暂无简介'}</p>
                </div>
                
                <div class="detail-actions">
                    <button id="viewCourseBtn" class="btn btn-primary">查看课程详情</button>
                </div>
            </div>
        `;
        
        // 创建对话框
        const dialog = UI.createDialog('课程信息', content, '500px');
        
        // 绑定查看课程详情按钮
        const viewCourseBtn = dialog.body.querySelector('#viewCourseBtn');
        if (viewCourseBtn) {
            viewCourseBtn.addEventListener('click', () => {
                dialog.close();
                // 跳转到课程详情页面
                window.location.href = `course-detail.html?id=${course.courseId}`;
            });
        }
    },
    
    /**
     * 格式化周次范围
     * @param {Array} weeks 周次数组 [1, 2, 3, 5, 6, 7, 9, 10]
     * @returns {string} 格式化后的周次范围 "1-3周, 5-7周, 9-10周"
     */
    formatWeekRange: function(weeks) {
        if (!weeks || weeks.length === 0) return '无';
        
        // 排序
        weeks.sort((a, b) => a - b);
        
        // 找连续范围
        const ranges = [];
        let start = weeks[0];
        let end = weeks[0];
        
        for (let i = 1; i < weeks.length; i++) {
            if (weeks[i] === end + 1) {
                // 连续
                end = weeks[i];
            } else {
                // 不连续，保存前一段
                ranges.push(start === end ? `${start}` : `${start}-${end}`);
                // 开始新的范围
                start = end = weeks[i];
            }
        }
        
        // 添加最后一段
        ranges.push(start === end ? `${start}` : `${start}-${end}`);
        
        return ranges.map(range => `${range}周`).join(', ');
    },
    
    /**
     * 绑定课表事件
     * @param {HTMLElement} container 容器元素
     */
    bindScheduleEvents: function(container) {
        // 上一周/月按钮
        const prevBtn = container.querySelector('.prev-btn');
        if (prevBtn) {
            prevBtn.addEventListener('click', () => {
                if (this.options.viewType === 'week') {
                    this.options.currentWeek = Math.max(1, this.options.currentWeek - 1);
                } else {
                    // 月视图逻辑
                }
                
                // 重新渲染
                this.initSchedule(container.id, this.options);
            });
        }
        
        // 下一周/月按钮
        const nextBtn = container.querySelector('.next-btn');
        if (nextBtn) {
            nextBtn.addEventListener('click', () => {
                if (this.options.viewType === 'week') {
                    this.options.currentWeek = Math.min(this.options.weekCount, this.options.currentWeek + 1);
                } else {
                    // 月视图逻辑
                }
                
                // 重新渲染
                this.initSchedule(container.id, this.options);
            });
        }
        
        // 今天按钮
        const todayBtn = container.querySelector('.today-btn');
        if (todayBtn) {
            todayBtn.addEventListener('click', () => {
                if (this.options.viewType === 'week') {
                    // 计算当前是第几周
                    const today = new Date();
                    const firstDayOfSemester = this.getFirstDayOfSemester();
                    const diffDays = Math.floor((today - firstDayOfSemester) / (24 * 60 * 60 * 1000));
                    this.options.currentWeek = Math.floor(diffDays / 7) + 1;
                    
                    // 确保在有效范围内
                    this.options.currentWeek = Math.max(1, Math.min(this.options.weekCount, this.options.currentWeek));
                } else {
                    // 月视图逻辑
                }
                
                // 重新渲染
                this.initSchedule(container.id, this.options);
            });
        }
        
        // 视图切换按钮
        const viewButtons = container.querySelectorAll('.view-switch button');
        viewButtons.forEach(button => {
            button.addEventListener('click', () => {
                const view = button.getAttribute('data-view');
                
                // 切换视图类型
                this.options.viewType = view;
                
                // 更新按钮状态
                viewButtons.forEach(btn => {
                    btn.classList.remove('btn-primary');
                    btn.classList.add('btn-outline-primary');
                });
                button.classList.remove('btn-outline-primary');
                button.classList.add('btn-primary');
                
                // 重新渲染
                this.initSchedule(container.id, this.options);
            });
        });
    }
};

// 将UISchedule添加到全局UI对象中
if (typeof UI !== 'undefined') {
    UI.schedule = UISchedule;
} 