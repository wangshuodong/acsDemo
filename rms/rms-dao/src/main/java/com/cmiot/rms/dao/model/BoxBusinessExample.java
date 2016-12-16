package com.cmiot.rms.dao.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BoxBusinessExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public BoxBusinessExample() {
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

        public Criteria andBoxUuidIsNull() {
            addCriterion("box_uuid is null");
            return (Criteria) this;
        }

        public Criteria andBoxUuidIsNotNull() {
            addCriterion("box_uuid is not null");
            return (Criteria) this;
        }

        public Criteria andBoxUuidEqualTo(String value) {
            addCriterion("box_uuid =", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidNotEqualTo(String value) {
            addCriterion("box_uuid <>", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidGreaterThan(String value) {
            addCriterion("box_uuid >", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidGreaterThanOrEqualTo(String value) {
            addCriterion("box_uuid >=", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidLessThan(String value) {
            addCriterion("box_uuid <", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidLessThanOrEqualTo(String value) {
            addCriterion("box_uuid <=", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidLike(String value) {
            addCriterion("box_uuid like", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidNotLike(String value) {
            addCriterion("box_uuid not like", value, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidIn(List<String> values) {
            addCriterion("box_uuid in", values, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidNotIn(List<String> values) {
            addCriterion("box_uuid not in", values, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidBetween(String value1, String value2) {
            addCriterion("box_uuid between", value1, value2, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBoxUuidNotBetween(String value1, String value2) {
            addCriterion("box_uuid not between", value1, value2, "boxUuid");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossIsNull() {
            addCriterion("business_code_boss is null");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossIsNotNull() {
            addCriterion("business_code_boss is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossEqualTo(String value) {
            addCriterion("business_code_boss =", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossNotEqualTo(String value) {
            addCriterion("business_code_boss <>", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossGreaterThan(String value) {
            addCriterion("business_code_boss >", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossGreaterThanOrEqualTo(String value) {
            addCriterion("business_code_boss >=", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossLessThan(String value) {
            addCriterion("business_code_boss <", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossLessThanOrEqualTo(String value) {
            addCriterion("business_code_boss <=", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossLike(String value) {
            addCriterion("business_code_boss like", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossNotLike(String value) {
            addCriterion("business_code_boss not like", value, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossIn(List<String> values) {
            addCriterion("business_code_boss in", values, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossNotIn(List<String> values) {
            addCriterion("business_code_boss not in", values, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossBetween(String value1, String value2) {
            addCriterion("business_code_boss between", value1, value2, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBossNotBetween(String value1, String value2) {
            addCriterion("business_code_boss not between", value1, value2, "businessCodeBoss");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeIsNull() {
            addCriterion("business_code is null");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeIsNotNull() {
            addCriterion("business_code is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeEqualTo(String value) {
            addCriterion("business_code =", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeNotEqualTo(String value) {
            addCriterion("business_code <>", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeGreaterThan(String value) {
            addCriterion("business_code >", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeGreaterThanOrEqualTo(String value) {
            addCriterion("business_code >=", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeLessThan(String value) {
            addCriterion("business_code <", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeLessThanOrEqualTo(String value) {
            addCriterion("business_code <=", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeLike(String value) {
            addCriterion("business_code like", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeNotLike(String value) {
            addCriterion("business_code not like", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeIn(List<String> values) {
            addCriterion("business_code in", values, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeNotIn(List<String> values) {
            addCriterion("business_code not in", values, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBetween(String value1, String value2) {
            addCriterion("business_code between", value1, value2, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeNotBetween(String value1, String value2) {
            addCriterion("business_code not between", value1, value2, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessNameIsNull() {
            addCriterion("business_name is null");
            return (Criteria) this;
        }

        public Criteria andBusinessNameIsNotNull() {
            addCriterion("business_name is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessNameEqualTo(String value) {
            addCriterion("business_name =", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameNotEqualTo(String value) {
            addCriterion("business_name <>", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameGreaterThan(String value) {
            addCriterion("business_name >", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameGreaterThanOrEqualTo(String value) {
            addCriterion("business_name >=", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameLessThan(String value) {
            addCriterion("business_name <", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameLessThanOrEqualTo(String value) {
            addCriterion("business_name <=", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameLike(String value) {
            addCriterion("business_name like", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameNotLike(String value) {
            addCriterion("business_name not like", value, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameIn(List<String> values) {
            addCriterion("business_name in", values, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameNotIn(List<String> values) {
            addCriterion("business_name not in", values, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameBetween(String value1, String value2) {
            addCriterion("business_name between", value1, value2, "businessName");
            return (Criteria) this;
        }

        public Criteria andBusinessNameNotBetween(String value1, String value2) {
            addCriterion("business_name not between", value1, value2, "businessName");
            return (Criteria) this;
        }

        public Criteria andParameterListIsNull() {
            addCriterion("parameter_list is null");
            return (Criteria) this;
        }

        public Criteria andParameterListIsNotNull() {
            addCriterion("parameter_list is not null");
            return (Criteria) this;
        }

        public Criteria andParameterListEqualTo(String value) {
            addCriterion("parameter_list =", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListNotEqualTo(String value) {
            addCriterion("parameter_list <>", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListGreaterThan(String value) {
            addCriterion("parameter_list >", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListGreaterThanOrEqualTo(String value) {
            addCriterion("parameter_list >=", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListLessThan(String value) {
            addCriterion("parameter_list <", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListLessThanOrEqualTo(String value) {
            addCriterion("parameter_list <=", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListLike(String value) {
            addCriterion("parameter_list like", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListNotLike(String value) {
            addCriterion("parameter_list not like", value, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListIn(List<String> values) {
            addCriterion("parameter_list in", values, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListNotIn(List<String> values) {
            addCriterion("parameter_list not in", values, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListBetween(String value1, String value2) {
            addCriterion("parameter_list between", value1, value2, "parameterList");
            return (Criteria) this;
        }

        public Criteria andParameterListNotBetween(String value1, String value2) {
            addCriterion("parameter_list not between", value1, value2, "parameterList");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuIsNull() {
            addCriterion("business_statu is null");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuIsNotNull() {
            addCriterion("business_statu is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuEqualTo(String value) {
            addCriterion("business_statu =", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuNotEqualTo(String value) {
            addCriterion("business_statu <>", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuGreaterThan(String value) {
            addCriterion("business_statu >", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuGreaterThanOrEqualTo(String value) {
            addCriterion("business_statu >=", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuLessThan(String value) {
            addCriterion("business_statu <", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuLessThanOrEqualTo(String value) {
            addCriterion("business_statu <=", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuLike(String value) {
            addCriterion("business_statu like", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuNotLike(String value) {
            addCriterion("business_statu not like", value, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuIn(List<String> values) {
            addCriterion("business_statu in", values, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuNotIn(List<String> values) {
            addCriterion("business_statu not in", values, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuBetween(String value1, String value2) {
            addCriterion("business_statu between", value1, value2, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andBusinessStatuNotBetween(String value1, String value2) {
            addCriterion("business_statu not between", value1, value2, "businessStatu");
            return (Criteria) this;
        }

        public Criteria andAreacodeIsNull() {
            addCriterion("areacode is null");
            return (Criteria) this;
        }

        public Criteria andAreacodeIsNotNull() {
            addCriterion("areacode is not null");
            return (Criteria) this;
        }

        public Criteria andAreacodeEqualTo(String value) {
            addCriterion("areacode =", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeNotEqualTo(String value) {
            addCriterion("areacode <>", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeGreaterThan(String value) {
            addCriterion("areacode >", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeGreaterThanOrEqualTo(String value) {
            addCriterion("areacode >=", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeLessThan(String value) {
            addCriterion("areacode <", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeLessThanOrEqualTo(String value) {
            addCriterion("areacode <=", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeLike(String value) {
            addCriterion("areacode like", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeNotLike(String value) {
            addCriterion("areacode not like", value, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeIn(List<String> values) {
            addCriterion("areacode in", values, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeNotIn(List<String> values) {
            addCriterion("areacode not in", values, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeBetween(String value1, String value2) {
            addCriterion("areacode between", value1, value2, "areacode");
            return (Criteria) this;
        }

        public Criteria andAreacodeNotBetween(String value1, String value2) {
            addCriterion("areacode not between", value1, value2, "areacode");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Integer value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Integer value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Integer value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Integer value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Integer value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Integer> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Integer> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Integer value1, Integer value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeIsNull() {
            addCriterion("business_type is null");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeIsNotNull() {
            addCriterion("business_type is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeEqualTo(String value) {
            addCriterion("business_type =", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeNotEqualTo(String value) {
            addCriterion("business_type <>", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeGreaterThan(String value) {
            addCriterion("business_type >", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeGreaterThanOrEqualTo(String value) {
            addCriterion("business_type >=", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeLessThan(String value) {
            addCriterion("business_type <", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeLessThanOrEqualTo(String value) {
            addCriterion("business_type <=", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeLike(String value) {
            addCriterion("business_type like", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeNotLike(String value) {
            addCriterion("business_type not like", value, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeIn(List<String> values) {
            addCriterion("business_type in", values, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeNotIn(List<String> values) {
            addCriterion("business_type not in", values, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeBetween(String value1, String value2) {
            addCriterion("business_type between", value1, value2, "businessType");
            return (Criteria) this;
        }

        public Criteria andBusinessTypeNotBetween(String value1, String value2) {
            addCriterion("business_type not between", value1, value2, "businessType");
            return (Criteria) this;
        }

        public Criteria andBoxMacIsNull() {
            addCriterion("box_mac is null");
            return (Criteria) this;
        }

        public Criteria andBoxMacIsNotNull() {
            addCriterion("box_mac is not null");
            return (Criteria) this;
        }

        public Criteria andBoxMacEqualTo(String value) {
            addCriterion("box_mac =", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacNotEqualTo(String value) {
            addCriterion("box_mac <>", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacGreaterThan(String value) {
            addCriterion("box_mac >", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacGreaterThanOrEqualTo(String value) {
            addCriterion("box_mac >=", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacLessThan(String value) {
            addCriterion("box_mac <", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacLessThanOrEqualTo(String value) {
            addCriterion("box_mac <=", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacLike(String value) {
            addCriterion("box_mac like", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacNotLike(String value) {
            addCriterion("box_mac not like", value, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacIn(List<String> values) {
            addCriterion("box_mac in", values, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacNotIn(List<String> values) {
            addCriterion("box_mac not in", values, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacBetween(String value1, String value2) {
            addCriterion("box_mac between", value1, value2, "boxMac");
            return (Criteria) this;
        }

        public Criteria andBoxMacNotBetween(String value1, String value2) {
            addCriterion("box_mac not between", value1, value2, "boxMac");
            return (Criteria) this;
        }

        public Criteria andOrderNoIsNull() {
            addCriterion("order_no is null");
            return (Criteria) this;
        }

        public Criteria andOrderNoIsNotNull() {
            addCriterion("order_no is not null");
            return (Criteria) this;
        }

        public Criteria andOrderNoEqualTo(String value) {
            addCriterion("order_no =", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoNotEqualTo(String value) {
            addCriterion("order_no <>", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoGreaterThan(String value) {
            addCriterion("order_no >", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoGreaterThanOrEqualTo(String value) {
            addCriterion("order_no >=", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoLessThan(String value) {
            addCriterion("order_no <", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoLessThanOrEqualTo(String value) {
            addCriterion("order_no <=", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoLike(String value) {
            addCriterion("order_no like", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoNotLike(String value) {
            addCriterion("order_no not like", value, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoIn(List<String> values) {
            addCriterion("order_no in", values, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoNotIn(List<String> values) {
            addCriterion("order_no not in", values, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoBetween(String value1, String value2) {
            addCriterion("order_no between", value1, value2, "orderNo");
            return (Criteria) this;
        }

        public Criteria andOrderNoNotBetween(String value1, String value2) {
            addCriterion("order_no not between", value1, value2, "orderNo");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Integer value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Integer value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Integer value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Integer value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Integer value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Integer> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Integer> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Integer value1, Integer value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andFailCountIsNull() {
            addCriterion("fail_count is null");
            return (Criteria) this;
        }

        public Criteria andFailCountIsNotNull() {
            addCriterion("fail_count is not null");
            return (Criteria) this;
        }

        public Criteria andFailCountEqualTo(Integer value) {
            addCriterion("fail_count =", value, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountNotEqualTo(Integer value) {
            addCriterion("fail_count <>", value, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountGreaterThan(Integer value) {
            addCriterion("fail_count >", value, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("fail_count >=", value, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountLessThan(Integer value) {
            addCriterion("fail_count <", value, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountLessThanOrEqualTo(Integer value) {
            addCriterion("fail_count <=", value, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountIn(List<Integer> values) {
            addCriterion("fail_count in", values, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountNotIn(List<Integer> values) {
            addCriterion("fail_count not in", values, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountBetween(Integer value1, Integer value2) {
            addCriterion("fail_count between", value1, value2, "failCount");
            return (Criteria) this;
        }

        public Criteria andFailCountNotBetween(Integer value1, Integer value2) {
            addCriterion("fail_count not between", value1, value2, "failCount");
            return (Criteria) this;
        }

        public Criteria andProvcodeIsNull() {
            addCriterion("provCode is null");
            return (Criteria) this;
        }

        public Criteria andProvcodeIsNotNull() {
            addCriterion("provCode is not null");
            return (Criteria) this;
        }

        public Criteria andProvcodeEqualTo(String value) {
            addCriterion("provCode =", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeNotEqualTo(String value) {
            addCriterion("provCode <>", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeGreaterThan(String value) {
            addCriterion("provCode >", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeGreaterThanOrEqualTo(String value) {
            addCriterion("provCode >=", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeLessThan(String value) {
            addCriterion("provCode <", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeLessThanOrEqualTo(String value) {
            addCriterion("provCode <=", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeLike(String value) {
            addCriterion("provCode like", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeNotLike(String value) {
            addCriterion("provCode not like", value, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeIn(List<String> values) {
            addCriterion("provCode in", values, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeNotIn(List<String> values) {
            addCriterion("provCode not in", values, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeBetween(String value1, String value2) {
            addCriterion("provCode between", value1, value2, "provcode");
            return (Criteria) this;
        }

        public Criteria andProvcodeNotBetween(String value1, String value2) {
            addCriterion("provCode not between", value1, value2, "provcode");
            return (Criteria) this;
        }

        public Criteria andUseridBossIsNull() {
            addCriterion("userId_boss is null");
            return (Criteria) this;
        }

        public Criteria andUseridBossIsNotNull() {
            addCriterion("userId_boss is not null");
            return (Criteria) this;
        }

        public Criteria andUseridBossEqualTo(String value) {
            addCriterion("userId_boss =", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossNotEqualTo(String value) {
            addCriterion("userId_boss <>", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossGreaterThan(String value) {
            addCriterion("userId_boss >", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossGreaterThanOrEqualTo(String value) {
            addCriterion("userId_boss >=", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossLessThan(String value) {
            addCriterion("userId_boss <", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossLessThanOrEqualTo(String value) {
            addCriterion("userId_boss <=", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossLike(String value) {
            addCriterion("userId_boss like", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossNotLike(String value) {
            addCriterion("userId_boss not like", value, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossIn(List<String> values) {
            addCriterion("userId_boss in", values, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossNotIn(List<String> values) {
            addCriterion("userId_boss not in", values, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossBetween(String value1, String value2) {
            addCriterion("userId_boss between", value1, value2, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andUseridBossNotBetween(String value1, String value2) {
            addCriterion("userId_boss not between", value1, value2, "useridBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossIsNull() {
            addCriterion("orderTime_boss is null");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossIsNotNull() {
            addCriterion("orderTime_boss is not null");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossEqualTo(Date value) {
            addCriterion("orderTime_boss =", value, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossNotEqualTo(Date value) {
            addCriterion("orderTime_boss <>", value, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossGreaterThan(Date value) {
            addCriterion("orderTime_boss >", value, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossGreaterThanOrEqualTo(Date value) {
            addCriterion("orderTime_boss >=", value, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossLessThan(Date value) {
            addCriterion("orderTime_boss <", value, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossLessThanOrEqualTo(Date value) {
            addCriterion("orderTime_boss <=", value, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossIn(List<Date> values) {
            addCriterion("orderTime_boss in", values, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossNotIn(List<Date> values) {
            addCriterion("orderTime_boss not in", values, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossBetween(Date value1, Date value2) {
            addCriterion("orderTime_boss between", value1, value2, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andOrdertimeBossNotBetween(Date value1, Date value2) {
            addCriterion("orderTime_boss not between", value1, value2, "ordertimeBoss");
            return (Criteria) this;
        }

        public Criteria andDevicetypeIsNull() {
            addCriterion("deviceType is null");
            return (Criteria) this;
        }

        public Criteria andDevicetypeIsNotNull() {
            addCriterion("deviceType is not null");
            return (Criteria) this;
        }

        public Criteria andDevicetypeEqualTo(String value) {
            addCriterion("deviceType =", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeNotEqualTo(String value) {
            addCriterion("deviceType <>", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeGreaterThan(String value) {
            addCriterion("deviceType >", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeGreaterThanOrEqualTo(String value) {
            addCriterion("deviceType >=", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeLessThan(String value) {
            addCriterion("deviceType <", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeLessThanOrEqualTo(String value) {
            addCriterion("deviceType <=", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeLike(String value) {
            addCriterion("deviceType like", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeNotLike(String value) {
            addCriterion("deviceType not like", value, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeIn(List<String> values) {
            addCriterion("deviceType in", values, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeNotIn(List<String> values) {
            addCriterion("deviceType not in", values, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeBetween(String value1, String value2) {
            addCriterion("deviceType between", value1, value2, "devicetype");
            return (Criteria) this;
        }

        public Criteria andDevicetypeNotBetween(String value1, String value2) {
            addCriterion("deviceType not between", value1, value2, "devicetype");
            return (Criteria) this;
        }

        public Criteria andServicemodeIsNull() {
            addCriterion("serviceMode is null");
            return (Criteria) this;
        }

        public Criteria andServicemodeIsNotNull() {
            addCriterion("serviceMode is not null");
            return (Criteria) this;
        }

        public Criteria andServicemodeEqualTo(String value) {
            addCriterion("serviceMode =", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeNotEqualTo(String value) {
            addCriterion("serviceMode <>", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeGreaterThan(String value) {
            addCriterion("serviceMode >", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeGreaterThanOrEqualTo(String value) {
            addCriterion("serviceMode >=", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeLessThan(String value) {
            addCriterion("serviceMode <", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeLessThanOrEqualTo(String value) {
            addCriterion("serviceMode <=", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeLike(String value) {
            addCriterion("serviceMode like", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeNotLike(String value) {
            addCriterion("serviceMode not like", value, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeIn(List<String> values) {
            addCriterion("serviceMode in", values, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeNotIn(List<String> values) {
            addCriterion("serviceMode not in", values, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeBetween(String value1, String value2) {
            addCriterion("serviceMode between", value1, value2, "servicemode");
            return (Criteria) this;
        }

        public Criteria andServicemodeNotBetween(String value1, String value2) {
            addCriterion("serviceMode not between", value1, value2, "servicemode");
            return (Criteria) this;
        }

        public Criteria andUsernameBossIsNull() {
            addCriterion("userName_boss is null");
            return (Criteria) this;
        }

        public Criteria andUsernameBossIsNotNull() {
            addCriterion("userName_boss is not null");
            return (Criteria) this;
        }

        public Criteria andUsernameBossEqualTo(String value) {
            addCriterion("userName_boss =", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossNotEqualTo(String value) {
            addCriterion("userName_boss <>", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossGreaterThan(String value) {
            addCriterion("userName_boss >", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossGreaterThanOrEqualTo(String value) {
            addCriterion("userName_boss >=", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossLessThan(String value) {
            addCriterion("userName_boss <", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossLessThanOrEqualTo(String value) {
            addCriterion("userName_boss <=", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossLike(String value) {
            addCriterion("userName_boss like", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossNotLike(String value) {
            addCriterion("userName_boss not like", value, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossIn(List<String> values) {
            addCriterion("userName_boss in", values, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossNotIn(List<String> values) {
            addCriterion("userName_boss not in", values, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossBetween(String value1, String value2) {
            addCriterion("userName_boss between", value1, value2, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUsernameBossNotBetween(String value1, String value2) {
            addCriterion("userName_boss not between", value1, value2, "usernameBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossIsNull() {
            addCriterion("userAddress_boss is null");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossIsNotNull() {
            addCriterion("userAddress_boss is not null");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossEqualTo(String value) {
            addCriterion("userAddress_boss =", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossNotEqualTo(String value) {
            addCriterion("userAddress_boss <>", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossGreaterThan(String value) {
            addCriterion("userAddress_boss >", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossGreaterThanOrEqualTo(String value) {
            addCriterion("userAddress_boss >=", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossLessThan(String value) {
            addCriterion("userAddress_boss <", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossLessThanOrEqualTo(String value) {
            addCriterion("userAddress_boss <=", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossLike(String value) {
            addCriterion("userAddress_boss like", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossNotLike(String value) {
            addCriterion("userAddress_boss not like", value, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossIn(List<String> values) {
            addCriterion("userAddress_boss in", values, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossNotIn(List<String> values) {
            addCriterion("userAddress_boss not in", values, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossBetween(String value1, String value2) {
            addCriterion("userAddress_boss between", value1, value2, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andUseraddressBossNotBetween(String value1, String value2) {
            addCriterion("userAddress_boss not between", value1, value2, "useraddressBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossIsNull() {
            addCriterion("contactPerson_boss is null");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossIsNotNull() {
            addCriterion("contactPerson_boss is not null");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossEqualTo(String value) {
            addCriterion("contactPerson_boss =", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossNotEqualTo(String value) {
            addCriterion("contactPerson_boss <>", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossGreaterThan(String value) {
            addCriterion("contactPerson_boss >", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossGreaterThanOrEqualTo(String value) {
            addCriterion("contactPerson_boss >=", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossLessThan(String value) {
            addCriterion("contactPerson_boss <", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossLessThanOrEqualTo(String value) {
            addCriterion("contactPerson_boss <=", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossLike(String value) {
            addCriterion("contactPerson_boss like", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossNotLike(String value) {
            addCriterion("contactPerson_boss not like", value, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossIn(List<String> values) {
            addCriterion("contactPerson_boss in", values, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossNotIn(List<String> values) {
            addCriterion("contactPerson_boss not in", values, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossBetween(String value1, String value2) {
            addCriterion("contactPerson_boss between", value1, value2, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactpersonBossNotBetween(String value1, String value2) {
            addCriterion("contactPerson_boss not between", value1, value2, "contactpersonBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossIsNull() {
            addCriterion("contactManner_boss is null");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossIsNotNull() {
            addCriterion("contactManner_boss is not null");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossEqualTo(String value) {
            addCriterion("contactManner_boss =", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossNotEqualTo(String value) {
            addCriterion("contactManner_boss <>", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossGreaterThan(String value) {
            addCriterion("contactManner_boss >", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossGreaterThanOrEqualTo(String value) {
            addCriterion("contactManner_boss >=", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossLessThan(String value) {
            addCriterion("contactManner_boss <", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossLessThanOrEqualTo(String value) {
            addCriterion("contactManner_boss <=", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossLike(String value) {
            addCriterion("contactManner_boss like", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossNotLike(String value) {
            addCriterion("contactManner_boss not like", value, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossIn(List<String> values) {
            addCriterion("contactManner_boss in", values, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossNotIn(List<String> values) {
            addCriterion("contactManner_boss not in", values, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossBetween(String value1, String value2) {
            addCriterion("contactManner_boss between", value1, value2, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andContactmannerBossNotBetween(String value1, String value2) {
            addCriterion("contactManner_boss not between", value1, value2, "contactmannerBoss");
            return (Criteria) this;
        }

        public Criteria andIpoeidIsNull() {
            addCriterion("ipoeId is null");
            return (Criteria) this;
        }

        public Criteria andIpoeidIsNotNull() {
            addCriterion("ipoeId is not null");
            return (Criteria) this;
        }

        public Criteria andIpoeidEqualTo(String value) {
            addCriterion("ipoeId =", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidNotEqualTo(String value) {
            addCriterion("ipoeId <>", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidGreaterThan(String value) {
            addCriterion("ipoeId >", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidGreaterThanOrEqualTo(String value) {
            addCriterion("ipoeId >=", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidLessThan(String value) {
            addCriterion("ipoeId <", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidLessThanOrEqualTo(String value) {
            addCriterion("ipoeId <=", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidLike(String value) {
            addCriterion("ipoeId like", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidNotLike(String value) {
            addCriterion("ipoeId not like", value, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidIn(List<String> values) {
            addCriterion("ipoeId in", values, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidNotIn(List<String> values) {
            addCriterion("ipoeId not in", values, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidBetween(String value1, String value2) {
            addCriterion("ipoeId between", value1, value2, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoeidNotBetween(String value1, String value2) {
            addCriterion("ipoeId not between", value1, value2, "ipoeid");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordIsNull() {
            addCriterion("ipoePassword is null");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordIsNotNull() {
            addCriterion("ipoePassword is not null");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordEqualTo(String value) {
            addCriterion("ipoePassword =", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordNotEqualTo(String value) {
            addCriterion("ipoePassword <>", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordGreaterThan(String value) {
            addCriterion("ipoePassword >", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordGreaterThanOrEqualTo(String value) {
            addCriterion("ipoePassword >=", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordLessThan(String value) {
            addCriterion("ipoePassword <", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordLessThanOrEqualTo(String value) {
            addCriterion("ipoePassword <=", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordLike(String value) {
            addCriterion("ipoePassword like", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordNotLike(String value) {
            addCriterion("ipoePassword not like", value, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordIn(List<String> values) {
            addCriterion("ipoePassword in", values, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordNotIn(List<String> values) {
            addCriterion("ipoePassword not in", values, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordBetween(String value1, String value2) {
            addCriterion("ipoePassword between", value1, value2, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andIpoepasswordNotBetween(String value1, String value2) {
            addCriterion("ipoePassword not between", value1, value2, "ipoepassword");
            return (Criteria) this;
        }

        public Criteria andUseridIsNull() {
            addCriterion("userId is null");
            return (Criteria) this;
        }

        public Criteria andUseridIsNotNull() {
            addCriterion("userId is not null");
            return (Criteria) this;
        }

        public Criteria andUseridEqualTo(String value) {
            addCriterion("userId =", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridNotEqualTo(String value) {
            addCriterion("userId <>", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridGreaterThan(String value) {
            addCriterion("userId >", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridGreaterThanOrEqualTo(String value) {
            addCriterion("userId >=", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridLessThan(String value) {
            addCriterion("userId <", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridLessThanOrEqualTo(String value) {
            addCriterion("userId <=", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridLike(String value) {
            addCriterion("userId like", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridNotLike(String value) {
            addCriterion("userId not like", value, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridIn(List<String> values) {
            addCriterion("userId in", values, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridNotIn(List<String> values) {
            addCriterion("userId not in", values, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridBetween(String value1, String value2) {
            addCriterion("userId between", value1, value2, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridNotBetween(String value1, String value2) {
            addCriterion("userId not between", value1, value2, "userid");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordIsNull() {
            addCriterion("userIdPassword is null");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordIsNotNull() {
            addCriterion("userIdPassword is not null");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordEqualTo(String value) {
            addCriterion("userIdPassword =", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordNotEqualTo(String value) {
            addCriterion("userIdPassword <>", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordGreaterThan(String value) {
            addCriterion("userIdPassword >", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordGreaterThanOrEqualTo(String value) {
            addCriterion("userIdPassword >=", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordLessThan(String value) {
            addCriterion("userIdPassword <", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordLessThanOrEqualTo(String value) {
            addCriterion("userIdPassword <=", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordLike(String value) {
            addCriterion("userIdPassword like", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordNotLike(String value) {
            addCriterion("userIdPassword not like", value, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordIn(List<String> values) {
            addCriterion("userIdPassword in", values, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordNotIn(List<String> values) {
            addCriterion("userIdPassword not in", values, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordBetween(String value1, String value2) {
            addCriterion("userIdPassword between", value1, value2, "useridpassword");
            return (Criteria) this;
        }

        public Criteria andUseridpasswordNotBetween(String value1, String value2) {
            addCriterion("userIdPassword not between", value1, value2, "useridpassword");
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