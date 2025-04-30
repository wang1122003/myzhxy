<style scoped>
.schedule-card {
  margin-top: 15px;
}

.week-view-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.week-view {
  display: grid;
  grid-template-columns: 60px repeat(7, 1fr);
  min-width: 600px;
  font-size: 14px;
}

.header-cell, .time-cell, .schedule-cell {
  min-height: 50px;
  font-size: 11px;
  padding: 6px;
}

.time-slot-label {
  font-size: 13px;
}

.course-item {
  padding: 6px;
  font-size: 13px;
}

.course-name {
  font-size: 14px;
}

@media (max-width: 768px) {
  .week-view {
    grid-template-columns: 45px repeat(7, 1fr);
    min-width: 400px;
    font-size: 13px;
  }

  .header-cell, .time-cell, .schedule-cell {
    min-height: 40px;
    padding: 4px;
  }

  .course-location, .course-teacher, .course-weeks {
    font-size: 9px;
  }
}
</style>