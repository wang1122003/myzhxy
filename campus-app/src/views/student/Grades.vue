<template>
  <div class="grades-page">
    <h1>成绩查询</h1>
    <div class="term-selector">
      <el-select v-model="selectedTerm" placeholder="请选择学期">
        <el-option
            v-for="item in terms"
            :key="item.value"
            :label="item.label"
            :value="item.value">
        </el-option>
      </el-select>
      <el-button type="primary" @click="queryGrades">查询</el-button>
    </div>

    <div v-loading="loading" class="grades-table">
      <el-table :data="gradesList" style="width: 100%">
        <el-table-column label="课程编号" prop="courseNo" width="120"></el-table-column>
        <el-table-column label="课程名称" prop="courseName" width="180"></el-table-column>
        <el-table-column label="学分" prop="credit" width="80"></el-table-column>
        <el-table-column label="成绩" prop="score"></el-table-column>
        <el-table-column label="绩点" prop="gradePoint"></el-table-column>
        <el-table-column label="考试时间" prop="examTime"></el-table-column>
      </el-table>

      <div v-if="gradesList.length > 0" class="grades-summary">
        <p>本学期平均绩点：<span class="gpa">{{ averageGPA }}</span></p>
        <p>获得学分：<span class="credit">{{ totalCredit }}</span></p>
      </div>

      <div v-if="!loading && gradesList.length === 0" class="empty-data">
        <p>暂无成绩数据</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentGrades',
  data() {
    return {
      loading: false,
      selectedTerm: '',
      terms: [
        {value: '2023-2024-1', label: '2023-2024学年第一学期'},
        {value: '2023-2024-2', label: '2023-2024学年第二学期'},
        {value: '2022-2023-1', label: '2022-2023学年第一学期'},
        {value: '2022-2023-2', label: '2022-2023学年第二学期'}
      ],
      gradesList: [],
      averageGPA: 0,
      totalCredit: 0
    }
  },
  created() {
    // 获取学期列表
  },
  methods: {
    queryGrades() {
      if (!this.selectedTerm) {
        this.$message.warning('请选择学期');
        return;
      }

      this.loading = true;
      // 模拟API请求
      setTimeout(() => {
        this.mockGradeData();
        this.loading = false;
        this.calculateStats();
      }, 800);
    },
    mockGradeData() {
      // 模拟成绩数据
      this.gradesList = [
        {courseNo: 'CS101', courseName: '计算机导论', credit: 2, score: 92, gradePoint: 4.0, examTime: '2023-12-25'},
        {courseNo: 'MA102', courseName: '高等数学', credit: 4, score: 85, gradePoint: 3.7, examTime: '2023-12-28'},
        {courseNo: 'EN103', courseName: '大学英语', credit: 3, score: 88, gradePoint: 3.7, examTime: '2023-12-30'},
        {courseNo: 'PH104', courseName: '大学物理', credit: 4, score: 78, gradePoint: 3.0, examTime: '2024-01-05'}
      ];
    },
    calculateStats() {
      if (this.gradesList.length === 0) {
        this.averageGPA = 0;
        this.totalCredit = 0;
        return;
      }

      let totalWeightedGP = 0;
      let totalCredits = 0;

      this.gradesList.forEach(grade => {
        totalWeightedGP += grade.gradePoint * grade.credit;
        totalCredits += grade.credit;
      });

      this.averageGPA = (totalWeightedGP / totalCredits).toFixed(2);
      this.totalCredit = totalCredits;
    }
  }
}
</script>

<style scoped>
.grades-page {
  padding: 20px;
}

.term-selector {
  margin: 20px 0;
  display: flex;
  align-items: center;
}

.term-selector .el-select {
  width: 280px;
  margin-right: 15px;
}

.grades-table {
  margin-top: 20px;
}

.grades-summary {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.grades-summary p {
  margin: 5px 0;
}

.grades-summary .gpa, .grades-summary .credit {
  font-weight: bold;
  color: #409EFF;
}

.empty-data {
  text-align: center;
  padding: 30px;
  color: #999;
}
</style> 