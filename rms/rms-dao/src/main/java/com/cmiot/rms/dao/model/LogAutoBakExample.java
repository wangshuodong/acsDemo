package com.cmiot.rms.dao.model;

import java.util.ArrayList;
import java.util.List;

public class LogAutoBakExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LogAutoBakExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIntervalDateIsNull() {
            addCriterion("interval_date is null");
            return (Criteria) this;
        }

        public Criteria andIntervalDateIsNotNull() {
            addCriterion("interval_date is not null");
            return (Criteria) this;
        }

        public Criteria andIntervalDateEqualTo(Integer value) {
            addCriterion("interval_date =", value, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateNotEqualTo(Integer value) {
            addCriterion("interval_date <>", value, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateGreaterThan(Integer value) {
            addCriterion("interval_date >", value, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateGreaterThanOrEqualTo(Integer value) {
            addCriterion("interval_date >=", value, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateLessThan(Integer value) {
            addCriterion("interval_date <", value, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateLessThanOrEqualTo(Integer value) {
            addCriterion("interval_date <=", value, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateIn(List<Integer> values) {
            addCriterion("interval_date in", values, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateNotIn(List<Integer> values) {
            addCriterion("interval_date not in", values, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateBetween(Integer value1, Integer value2) {
            addCriterion("interval_date between", value1, value2, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andIntervalDateNotBetween(Integer value1, Integer value2) {
            addCriterion("interval_date not between", value1, value2, "intervalDate");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeIsNull() {
            addCriterion("last_work_time is null");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeIsNotNull() {
            addCriterion("last_work_time is not null");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeEqualTo(Long value) {
            addCriterion("last_work_time =", value, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeNotEqualTo(Long value) {
            addCriterion("last_work_time <>", value, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeGreaterThan(Long value) {
            addCriterion("last_work_time >", value, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("last_work_time >=", value, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeLessThan(Long value) {
            addCriterion("last_work_time <", value, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeLessThanOrEqualTo(Long value) {
            addCriterion("last_work_time <=", value, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeIn(List<Long> values) {
            addCriterion("last_work_time in", values, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeNotIn(List<Long> values) {
            addCriterion("last_work_time not in", values, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeBetween(Long value1, Long value2) {
            addCriterion("last_work_time between", value1, value2, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andLastWorkTimeNotBetween(Long value1, Long value2) {
            addCriterion("last_work_time not between", value1, value2, "lastWorkTime");
            return (Criteria) this;
        }

        public Criteria andEffectiveIsNull() {
            addCriterion("effective is null");
            return (Criteria) this;
        }

        public Criteria andEffectiveIsNotNull() {
            addCriterion("effective is not null");
            return (Criteria) this;
        }

        public Criteria andEffectiveEqualTo(Boolean value) {
            addCriterion("effective =", value, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveNotEqualTo(Boolean value) {
            addCriterion("effective <>", value, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveGreaterThan(Boolean value) {
            addCriterion("effective >", value, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveGreaterThanOrEqualTo(Boolean value) {
            addCriterion("effective >=", value, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveLessThan(Boolean value) {
            addCriterion("effective <", value, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveLessThanOrEqualTo(Boolean value) {
            addCriterion("effective <=", value, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveIn(List<Boolean> values) {
            addCriterion("effective in", values, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveNotIn(List<Boolean> values) {
            addCriterion("effective not in", values, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveBetween(Boolean value1, Boolean value2) {
            addCriterion("effective between", value1, value2, "effective");
            return (Criteria) this;
        }

        public Criteria andEffectiveNotBetween(Boolean value1, Boolean value2) {
            addCriterion("effective not between", value1, value2, "effective");
            return (Criteria) this;
        }

        public Criteria andSavePathIsNull() {
            addCriterion("save_path is null");
            return (Criteria) this;
        }

        public Criteria andSavePathIsNotNull() {
            addCriterion("save_path is not null");
            return (Criteria) this;
        }

        public Criteria andSavePathEqualTo(String value) {
            addCriterion("save_path =", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathNotEqualTo(String value) {
            addCriterion("save_path <>", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathGreaterThan(String value) {
            addCriterion("save_path >", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathGreaterThanOrEqualTo(String value) {
            addCriterion("save_path >=", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathLessThan(String value) {
            addCriterion("save_path <", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathLessThanOrEqualTo(String value) {
            addCriterion("save_path <=", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathLike(String value) {
            addCriterion("save_path like", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathNotLike(String value) {
            addCriterion("save_path not like", value, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathIn(List<String> values) {
            addCriterion("save_path in", values, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathNotIn(List<String> values) {
            addCriterion("save_path not in", values, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathBetween(String value1, String value2) {
            addCriterion("save_path between", value1, value2, "savePath");
            return (Criteria) this;
        }

        public Criteria andSavePathNotBetween(String value1, String value2) {
            addCriterion("save_path not between", value1, value2, "savePath");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}