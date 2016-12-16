package com.cmiot.rms.dao.model;

import java.util.ArrayList;
import java.util.List;

public class GatewayNodeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public GatewayNodeExample() {
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

        public Criteria andFactoryCodeIsNull() {
            addCriterion("factory_code is null");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeIsNotNull() {
            addCriterion("factory_code is not null");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeEqualTo(String value) {
            addCriterion("factory_code =", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeNotEqualTo(String value) {
            addCriterion("factory_code <>", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeGreaterThan(String value) {
            addCriterion("factory_code >", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeGreaterThanOrEqualTo(String value) {
            addCriterion("factory_code >=", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeLessThan(String value) {
            addCriterion("factory_code <", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeLessThanOrEqualTo(String value) {
            addCriterion("factory_code <=", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeLike(String value) {
            addCriterion("factory_code like", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeNotLike(String value) {
            addCriterion("factory_code not like", value, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeIn(List<String> values) {
            addCriterion("factory_code in", values, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeNotIn(List<String> values) {
            addCriterion("factory_code not in", values, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeBetween(String value1, String value2) {
            addCriterion("factory_code between", value1, value2, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andFactoryCodeNotBetween(String value1, String value2) {
            addCriterion("factory_code not between", value1, value2, "factoryCode");
            return (Criteria) this;
        }

        public Criteria andHdVersionIsNull() {
            addCriterion("hd_version is null");
            return (Criteria) this;
        }

        public Criteria andHdVersionIsNotNull() {
            addCriterion("hd_version is not null");
            return (Criteria) this;
        }

        public Criteria andHdVersionEqualTo(String value) {
            addCriterion("hd_version =", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionNotEqualTo(String value) {
            addCriterion("hd_version <>", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionGreaterThan(String value) {
            addCriterion("hd_version >", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionGreaterThanOrEqualTo(String value) {
            addCriterion("hd_version >=", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionLessThan(String value) {
            addCriterion("hd_version <", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionLessThanOrEqualTo(String value) {
            addCriterion("hd_version <=", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionLike(String value) {
            addCriterion("hd_version like", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionNotLike(String value) {
            addCriterion("hd_version not like", value, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionIn(List<String> values) {
            addCriterion("hd_version in", values, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionNotIn(List<String> values) {
            addCriterion("hd_version not in", values, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionBetween(String value1, String value2) {
            addCriterion("hd_version between", value1, value2, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andHdVersionNotBetween(String value1, String value2) {
            addCriterion("hd_version not between", value1, value2, "hdVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionIsNull() {
            addCriterion("firmware_version is null");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionIsNotNull() {
            addCriterion("firmware_version is not null");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionEqualTo(String value) {
            addCriterion("firmware_version =", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionNotEqualTo(String value) {
            addCriterion("firmware_version <>", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionGreaterThan(String value) {
            addCriterion("firmware_version >", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionGreaterThanOrEqualTo(String value) {
            addCriterion("firmware_version >=", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionLessThan(String value) {
            addCriterion("firmware_version <", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionLessThanOrEqualTo(String value) {
            addCriterion("firmware_version <=", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionLike(String value) {
            addCriterion("firmware_version like", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionNotLike(String value) {
            addCriterion("firmware_version not like", value, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionIn(List<String> values) {
            addCriterion("firmware_version in", values, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionNotIn(List<String> values) {
            addCriterion("firmware_version not in", values, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionBetween(String value1, String value2) {
            addCriterion("firmware_version between", value1, value2, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andFirmwareVersionNotBetween(String value1, String value2) {
            addCriterion("firmware_version not between", value1, value2, "firmwareVersion");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeIsNull() {
            addCriterion("login_password_node is null");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeIsNotNull() {
            addCriterion("login_password_node is not null");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeEqualTo(String value) {
            addCriterion("login_password_node =", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeNotEqualTo(String value) {
            addCriterion("login_password_node <>", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeGreaterThan(String value) {
            addCriterion("login_password_node >", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeGreaterThanOrEqualTo(String value) {
            addCriterion("login_password_node >=", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeLessThan(String value) {
            addCriterion("login_password_node <", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeLessThanOrEqualTo(String value) {
            addCriterion("login_password_node <=", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeLike(String value) {
            addCriterion("login_password_node like", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeNotLike(String value) {
            addCriterion("login_password_node not like", value, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeIn(List<String> values) {
            addCriterion("login_password_node in", values, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeNotIn(List<String> values) {
            addCriterion("login_password_node not in", values, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeBetween(String value1, String value2) {
            addCriterion("login_password_node between", value1, value2, "loginPasswordNode");
            return (Criteria) this;
        }

        public Criteria andLoginPasswordNodeNotBetween(String value1, String value2) {
            addCriterion("login_password_node not between", value1, value2, "loginPasswordNode");
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