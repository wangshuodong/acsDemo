package com.cmiot.rms.dao.model;

import java.util.ArrayList;
import java.util.List;

public class AreaExample extends BaseBean{
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public AreaExample() {
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

        public Criteria andAreaTableIdIsNull() {
            addCriterion("AREA_TABLE_ID is null");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdIsNotNull() {
            addCriterion("AREA_TABLE_ID is not null");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdEqualTo(Integer value) {
            addCriterion("AREA_TABLE_ID =", value, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdNotEqualTo(Integer value) {
            addCriterion("AREA_TABLE_ID <>", value, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdGreaterThan(Integer value) {
            addCriterion("AREA_TABLE_ID >", value, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("AREA_TABLE_ID >=", value, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdLessThan(Integer value) {
            addCriterion("AREA_TABLE_ID <", value, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdLessThanOrEqualTo(Integer value) {
            addCriterion("AREA_TABLE_ID <=", value, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdIn(List<Integer> values) {
            addCriterion("AREA_TABLE_ID in", values, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdNotIn(List<Integer> values) {
            addCriterion("AREA_TABLE_ID not in", values, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdBetween(Integer value1, Integer value2) {
            addCriterion("AREA_TABLE_ID between", value1, value2, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaTableIdNotBetween(Integer value1, Integer value2) {
            addCriterion("AREA_TABLE_ID not between", value1, value2, "areaTableId");
            return (Criteria) this;
        }

        public Criteria andAreaIdIsNull() {
            addCriterion("AREA_ID is null");
            return (Criteria) this;
        }

        public Criteria andAreaIdIsNotNull() {
            addCriterion("AREA_ID is not null");
            return (Criteria) this;
        }

        public Criteria andAreaIdEqualTo(String value) {
            addCriterion("AREA_ID =", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdNotEqualTo(String value) {
            addCriterion("AREA_ID <>", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdGreaterThan(String value) {
            addCriterion("AREA_ID >", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdGreaterThanOrEqualTo(String value) {
            addCriterion("AREA_ID >=", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdLessThan(String value) {
            addCriterion("AREA_ID <", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdLessThanOrEqualTo(String value) {
            addCriterion("AREA_ID <=", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdLike(String value) {
            addCriterion("AREA_ID like", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdNotLike(String value) {
            addCriterion("AREA_ID not like", value, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdIn(List<String> values) {
            addCriterion("AREA_ID in", values, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdNotIn(List<String> values) {
            addCriterion("AREA_ID not in", values, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdBetween(String value1, String value2) {
            addCriterion("AREA_ID between", value1, value2, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaIdNotBetween(String value1, String value2) {
            addCriterion("AREA_ID not between", value1, value2, "areaId");
            return (Criteria) this;
        }

        public Criteria andAreaNameIsNull() {
            addCriterion("AREA_NAME is null");
            return (Criteria) this;
        }

        public Criteria andAreaNameIsNotNull() {
            addCriterion("AREA_NAME is not null");
            return (Criteria) this;
        }

        public Criteria andAreaNameEqualTo(String value) {
            addCriterion("AREA_NAME =", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameNotEqualTo(String value) {
            addCriterion("AREA_NAME <>", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameGreaterThan(String value) {
            addCriterion("AREA_NAME >", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameGreaterThanOrEqualTo(String value) {
            addCriterion("AREA_NAME >=", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameLessThan(String value) {
            addCriterion("AREA_NAME <", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameLessThanOrEqualTo(String value) {
            addCriterion("AREA_NAME <=", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameLike(String value) {
            addCriterion("AREA_NAME like", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameNotLike(String value) {
            addCriterion("AREA_NAME not like", value, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameIn(List<String> values) {
            addCriterion("AREA_NAME in", values, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameNotIn(List<String> values) {
            addCriterion("AREA_NAME not in", values, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameBetween(String value1, String value2) {
            addCriterion("AREA_NAME between", value1, value2, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaNameNotBetween(String value1, String value2) {
            addCriterion("AREA_NAME not between", value1, value2, "areaName");
            return (Criteria) this;
        }

        public Criteria andAreaTypeIsNull() {
            addCriterion("AREA_TYPE is null");
            return (Criteria) this;
        }

        public Criteria andAreaTypeIsNotNull() {
            addCriterion("AREA_TYPE is not null");
            return (Criteria) this;
        }

        public Criteria andAreaTypeEqualTo(String value) {
            addCriterion("AREA_TYPE =", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeNotEqualTo(String value) {
            addCriterion("AREA_TYPE <>", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeGreaterThan(String value) {
            addCriterion("AREA_TYPE >", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeGreaterThanOrEqualTo(String value) {
            addCriterion("AREA_TYPE >=", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeLessThan(String value) {
            addCriterion("AREA_TYPE <", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeLessThanOrEqualTo(String value) {
            addCriterion("AREA_TYPE <=", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeLike(String value) {
            addCriterion("AREA_TYPE like", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeNotLike(String value) {
            addCriterion("AREA_TYPE not like", value, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeIn(List<String> values) {
            addCriterion("AREA_TYPE in", values, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeNotIn(List<String> values) {
            addCriterion("AREA_TYPE not in", values, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeBetween(String value1, String value2) {
            addCriterion("AREA_TYPE between", value1, value2, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaTypeNotBetween(String value1, String value2) {
            addCriterion("AREA_TYPE not between", value1, value2, "areaType");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdIsNull() {
            addCriterion("AREA_PARENT_AREA_ID is null");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdIsNotNull() {
            addCriterion("AREA_PARENT_AREA_ID is not null");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdEqualTo(String value) {
            addCriterion("AREA_PARENT_AREA_ID =", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdNotEqualTo(String value) {
            addCriterion("AREA_PARENT_AREA_ID <>", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdGreaterThan(String value) {
            addCriterion("AREA_PARENT_AREA_ID >", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdGreaterThanOrEqualTo(String value) {
            addCriterion("AREA_PARENT_AREA_ID >=", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdLessThan(String value) {
            addCriterion("AREA_PARENT_AREA_ID <", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdLessThanOrEqualTo(String value) {
            addCriterion("AREA_PARENT_AREA_ID <=", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdLike(String value) {
            addCriterion("AREA_PARENT_AREA_ID like", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdNotLike(String value) {
            addCriterion("AREA_PARENT_AREA_ID not like", value, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdIn(List<String> values) {
            addCriterion("AREA_PARENT_AREA_ID in", values, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdNotIn(List<String> values) {
            addCriterion("AREA_PARENT_AREA_ID not in", values, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdBetween(String value1, String value2) {
            addCriterion("AREA_PARENT_AREA_ID between", value1, value2, "areaParentAreaId");
            return (Criteria) this;
        }

        public Criteria andAreaParentAreaIdNotBetween(String value1, String value2) {
            addCriterion("AREA_PARENT_AREA_ID not between", value1, value2, "areaParentAreaId");
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