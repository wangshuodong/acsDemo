package com.cmiot.rms.dao.model;

import java.util.ArrayList;
import java.util.List;

public class GatewayQueueExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public GatewayQueueExample() {
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

        public Criteria andGatewayQueueIdIsNull() {
            addCriterion("gateway_queue_id is null");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdIsNotNull() {
            addCriterion("gateway_queue_id is not null");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdEqualTo(String value) {
            addCriterion("gateway_queue_id =", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdNotEqualTo(String value) {
            addCriterion("gateway_queue_id <>", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdGreaterThan(String value) {
            addCriterion("gateway_queue_id >", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdGreaterThanOrEqualTo(String value) {
            addCriterion("gateway_queue_id >=", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdLessThan(String value) {
            addCriterion("gateway_queue_id <", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdLessThanOrEqualTo(String value) {
            addCriterion("gateway_queue_id <=", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdLike(String value) {
            addCriterion("gateway_queue_id like", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdNotLike(String value) {
            addCriterion("gateway_queue_id not like", value, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdIn(List<String> values) {
            addCriterion("gateway_queue_id in", values, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdNotIn(List<String> values) {
            addCriterion("gateway_queue_id not in", values, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdBetween(String value1, String value2) {
            addCriterion("gateway_queue_id between", value1, value2, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayQueueIdNotBetween(String value1, String value2) {
            addCriterion("gateway_queue_id not between", value1, value2, "gatewayQueueId");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidIsNull() {
            addCriterion("gateway_uuid is null");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidIsNotNull() {
            addCriterion("gateway_uuid is not null");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidEqualTo(String value) {
            addCriterion("gateway_uuid =", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidNotEqualTo(String value) {
            addCriterion("gateway_uuid <>", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidGreaterThan(String value) {
            addCriterion("gateway_uuid >", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidGreaterThanOrEqualTo(String value) {
            addCriterion("gateway_uuid >=", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidLessThan(String value) {
            addCriterion("gateway_uuid <", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidLessThanOrEqualTo(String value) {
            addCriterion("gateway_uuid <=", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidLike(String value) {
            addCriterion("gateway_uuid like", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidNotLike(String value) {
            addCriterion("gateway_uuid not like", value, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidIn(List<String> values) {
            addCriterion("gateway_uuid in", values, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidNotIn(List<String> values) {
            addCriterion("gateway_uuid not in", values, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidBetween(String value1, String value2) {
            addCriterion("gateway_uuid between", value1, value2, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andGatewayUuidNotBetween(String value1, String value2) {
            addCriterion("gateway_uuid not between", value1, value2, "gatewayUuid");
            return (Criteria) this;
        }

        public Criteria andSynStatuIsNull() {
            addCriterion("syn_statu is null");
            return (Criteria) this;
        }

        public Criteria andSynStatuIsNotNull() {
            addCriterion("syn_statu is not null");
            return (Criteria) this;
        }

        public Criteria andSynStatuEqualTo(Integer value) {
            addCriterion("syn_statu =", value, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuNotEqualTo(Integer value) {
            addCriterion("syn_statu <>", value, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuGreaterThan(Integer value) {
            addCriterion("syn_statu >", value, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuGreaterThanOrEqualTo(Integer value) {
            addCriterion("syn_statu >=", value, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuLessThan(Integer value) {
            addCriterion("syn_statu <", value, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuLessThanOrEqualTo(Integer value) {
            addCriterion("syn_statu <=", value, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuIn(List<Integer> values) {
            addCriterion("syn_statu in", values, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuNotIn(List<Integer> values) {
            addCriterion("syn_statu not in", values, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuBetween(Integer value1, Integer value2) {
            addCriterion("syn_statu between", value1, value2, "synStatu");
            return (Criteria) this;
        }

        public Criteria andSynStatuNotBetween(Integer value1, Integer value2) {
            addCriterion("syn_statu not between", value1, value2, "synStatu");
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