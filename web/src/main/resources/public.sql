/*
Navicat PGSQL Data Transfer

Source Server         : localhost
Source Server Version : 90601
Source Host           : localhost:5432
Source Database       : integration
Source Schema         : public

Target Server Type    : PGSQL
Target Server Version : 90601
File Encoding         : 65001

Date: 2017-06-24 12:03:06
*/


-- ----------------------------
-- Sequence structure for hibernate_sequence
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."hibernate_sequence";
CREATE SEQUENCE "public"."hibernate_sequence"
 INCREMENT 1
 MINVALUE 1
 MAXVALUE 9223372036854775807
 START 74
 CACHE 1;
SELECT setval('"public"."hibernate_sequence"', 74, true);

-- ----------------------------
-- Table structure for revinfo
-- ----------------------------
DROP TABLE IF EXISTS "public"."revinfo";
CREATE TABLE "public"."revinfo" (
"rev" int4 NOT NULL,
"revtstmp" int8
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of revinfo
-- ----------------------------
INSERT INTO "public"."revinfo" VALUES ('74', '1498276338156');

-- ----------------------------
-- Table structure for t_application_form
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_application_form";
CREATE TABLE "public"."t_application_form" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"cause" varchar(255) COLLATE "default",
"description" varchar(255) COLLATE "default" NOT NULL,
"name" varchar(255) COLLATE "default" NOT NULL,
"status" varchar(255) COLLATE "default" NOT NULL,
"user_applicant_id" int8,
"user_handler_id" int8
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_application_form
-- ----------------------------

-- ----------------------------
-- Table structure for t_application_handle_history
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_application_handle_history";
CREATE TABLE "public"."t_application_handle_history" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"cause" varchar(255) COLLATE "default",
"status" varchar(255) COLLATE "default",
"application_form_id" int8 NOT NULL,
"handler_id" int8
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_application_handle_history
-- ----------------------------

-- ----------------------------
-- Table structure for t_article
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_article";
CREATE TABLE "public"."t_article" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"is_approved" bool,
"body" text COLLATE "default" NOT NULL,
"is_comment" bool,
"cover" varchar(255) COLLATE "default",
"keywords" varchar(255) COLLATE "default",
"summary" text COLLATE "default",
"title" varchar(255) COLLATE "default" NOT NULL,
"author_id" int8 NOT NULL,
"article_type_id" int8
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_article
-- ----------------------------
INSERT INTO "public"."t_article" VALUES ('72', '2017-06-24 11:52:17.935', '2017-06-24 11:52:17.935', '0', 't', '39342', 't', null, '世界 您好 文章', '39343', '世界，您好！', '66', '71');

-- ----------------------------
-- Table structure for t_article_aud
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_article_aud";
CREATE TABLE "public"."t_article_aud" (
"id" int8 NOT NULL,
"rev" int4 NOT NULL,
"revtype" int2,
"body" text COLLATE "default",
"summary" text COLLATE "default",
"title" varchar(255) COLLATE "default"
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_article_aud
-- ----------------------------
INSERT INTO "public"."t_article_aud" VALUES ('72', '74', '0', '39345', '39346', '世界，您好！');

-- ----------------------------
-- Table structure for t_article_comment
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_article_comment";
CREATE TABLE "public"."t_article_comment" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"is_approved" bool,
"content" text COLLATE "default" NOT NULL,
"critics" varchar(255) COLLATE "default",
"icon" varchar(255) COLLATE "default",
"article_id" int8 NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_article_comment
-- ----------------------------
INSERT INTO "public"."t_article_comment" VALUES ('73', '2017-06-24 11:52:17.937', '2017-06-24 11:52:17.937', '0', 't', '39344', 'bar', 'download/img/icon-head-bar.jpg', '72');

-- ----------------------------
-- Table structure for t_article_type
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_article_type";
CREATE TABLE "public"."t_article_type" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"description" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default" NOT NULL,
"parent_type" int8
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_article_type
-- ----------------------------
INSERT INTO "public"."t_article_type" VALUES ('70', '2017-06-24 11:52:17.933', '2017-06-24 11:52:17.933', '0', null, '未分类', null);
INSERT INTO "public"."t_article_type" VALUES ('71', '2017-06-24 11:52:17.934', '2017-06-24 11:52:17.934', '0', null, '子类', '70');

-- ----------------------------
-- Table structure for t_authority
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_authority";
CREATE TABLE "public"."t_authority" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"description" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default" NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_authority
-- ----------------------------
INSERT INTO "public"."t_authority" VALUES ('38', '2017-06-24 11:52:17.85', '2017-06-24 11:52:17.85', '0', '对角色进行权限配置的权限', 'user_role_authority_allocation');
INSERT INTO "public"."t_authority" VALUES ('39', '2017-06-24 11:52:17.893', '2017-06-24 11:52:17.893', '0', '创建普通账号，用于用户自行注册时', 'user_create_ordinary');
INSERT INTO "public"."t_authority" VALUES ('40', '2017-06-24 11:52:17.894', '2017-06-24 11:52:17.894', '0', '创建有一定权限的账号，用于管理员创建时', 'user_create_special');
INSERT INTO "public"."t_authority" VALUES ('41', '2017-06-24 11:52:17.895', '2017-06-24 11:52:17.895', '0', '激活账号', 'user_enable');
INSERT INTO "public"."t_authority" VALUES ('42', '2017-06-24 11:52:17.896', '2017-06-24 11:52:17.896', '0', '禁用账号', 'user_disable');
INSERT INTO "public"."t_authority" VALUES ('43', '2017-06-24 11:52:17.896', '2017-06-24 11:52:17.896', '0', '授予角色', 'user_grant_roles');
INSERT INTO "public"."t_authority" VALUES ('44', '2017-06-24 11:52:17.898', '2017-06-24 11:52:17.898', '0', '读取所有用户的权限', 'user_read_all');
INSERT INTO "public"."t_authority" VALUES ('45', '2017-06-24 11:52:17.899', '2017-06-24 11:52:17.899', '0', '读取自己账号信息', 'user_read_self');
INSERT INTO "public"."t_authority" VALUES ('46', '2017-06-24 11:52:17.899', '2017-06-24 11:52:17.899', '0', '修改所有用户的权限，用于管理员', 'user_update_all');
INSERT INTO "public"."t_authority" VALUES ('47', '2017-06-24 11:52:17.9', '2017-06-24 11:52:17.9', '0', '修改自己账号的权限，用于普通用户', 'user_update_self');
INSERT INTO "public"."t_authority" VALUES ('48', '2017-06-24 11:52:17.9', '2017-06-24 11:52:17.9', '0', '删除用户的权限', 'user_delete');
INSERT INTO "public"."t_authority" VALUES ('49', '2017-06-24 11:52:17.901', '2017-06-24 11:52:17.901', '0', '客户管理的权限', 'user_customer');
INSERT INTO "public"."t_authority" VALUES ('50', '2017-06-24 11:52:17.904', '2017-06-24 11:52:17.904', '0', '处理申请单的权限', 'application_form_transit');
INSERT INTO "public"."t_authority" VALUES ('51', '2017-06-24 11:52:17.904', '2017-06-24 11:52:17.904', '0', '查看申请单历史记录的权限', 'application_form_read_history');
INSERT INTO "public"."t_authority" VALUES ('52', '2017-06-24 11:52:17.905', '2017-06-24 11:52:17.905', '0', '删除申请单', 'application_form_delete');
INSERT INTO "public"."t_authority" VALUES ('53', '2017-06-24 11:52:17.906', '2017-06-24 11:52:17.906', '0', '删除论坛帖子', 'forum_delete');
INSERT INTO "public"."t_authority" VALUES ('54', '2017-06-24 11:52:17.911', '2017-06-24 11:52:17.911', '0', '审计修改用户信息', 'audit_user');
INSERT INTO "public"."t_authority" VALUES ('55', '2017-06-24 11:52:17.912', '2017-06-24 11:52:17.912', '0', '审计修改角色信息', 'audit_role');
INSERT INTO "public"."t_authority" VALUES ('56', '2017-06-24 11:52:17.912', '2017-06-24 11:52:17.912', '0', '资源管理，文件上传，目录创建、改名以及删除', 'resource_manager');
INSERT INTO "public"."t_authority" VALUES ('57', '2017-06-24 11:52:17.912', '2017-06-24 11:52:17.912', '0', '内容管理', 'content_manager');

-- ----------------------------
-- Table structure for t_authority_aud
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_authority_aud";
CREATE TABLE "public"."t_authority_aud" (
"id" int8 NOT NULL,
"rev" int4 NOT NULL,
"revtype" int2,
"description" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default"
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_authority_aud
-- ----------------------------
INSERT INTO "public"."t_authority_aud" VALUES ('38', '74', '0', '对角色进行权限配置的权限', 'user_role_authority_allocation');
INSERT INTO "public"."t_authority_aud" VALUES ('39', '74', '0', '创建普通账号，用于用户自行注册时', 'user_create_ordinary');
INSERT INTO "public"."t_authority_aud" VALUES ('40', '74', '0', '创建有一定权限的账号，用于管理员创建时', 'user_create_special');
INSERT INTO "public"."t_authority_aud" VALUES ('41', '74', '0', '激活账号', 'user_enable');
INSERT INTO "public"."t_authority_aud" VALUES ('42', '74', '0', '禁用账号', 'user_disable');
INSERT INTO "public"."t_authority_aud" VALUES ('43', '74', '0', '授予角色', 'user_grant_roles');
INSERT INTO "public"."t_authority_aud" VALUES ('44', '74', '0', '读取所有用户的权限', 'user_read_all');
INSERT INTO "public"."t_authority_aud" VALUES ('45', '74', '0', '读取自己账号信息', 'user_read_self');
INSERT INTO "public"."t_authority_aud" VALUES ('46', '74', '0', '修改所有用户的权限，用于管理员', 'user_update_all');
INSERT INTO "public"."t_authority_aud" VALUES ('47', '74', '0', '修改自己账号的权限，用于普通用户', 'user_update_self');
INSERT INTO "public"."t_authority_aud" VALUES ('48', '74', '0', '删除用户的权限', 'user_delete');
INSERT INTO "public"."t_authority_aud" VALUES ('49', '74', '0', '客户管理的权限', 'user_customer');
INSERT INTO "public"."t_authority_aud" VALUES ('50', '74', '0', '处理申请单的权限', 'application_form_transit');
INSERT INTO "public"."t_authority_aud" VALUES ('51', '74', '0', '查看申请单历史记录的权限', 'application_form_read_history');
INSERT INTO "public"."t_authority_aud" VALUES ('52', '74', '0', '删除申请单', 'application_form_delete');
INSERT INTO "public"."t_authority_aud" VALUES ('53', '74', '0', '删除论坛帖子', 'forum_delete');
INSERT INTO "public"."t_authority_aud" VALUES ('54', '74', '0', '审计修改用户信息', 'audit_user');
INSERT INTO "public"."t_authority_aud" VALUES ('55', '74', '0', '审计修改角色信息', 'audit_role');
INSERT INTO "public"."t_authority_aud" VALUES ('56', '74', '0', '资源管理，文件上传，目录创建、改名以及删除', 'resource_manager');
INSERT INTO "public"."t_authority_aud" VALUES ('57', '74', '0', '内容管理', 'content_manager');

-- ----------------------------
-- Table structure for t_company
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_company";
CREATE TABLE "public"."t_company" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"description" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default"
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_company
-- ----------------------------
INSERT INTO "public"."t_company" VALUES ('62', '2017-06-24 11:52:17.915', '2017-06-24 11:52:17.915', '0', '公司上面还有集团公司', 'XXX注册公司');

-- ----------------------------
-- Table structure for t_department
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_department";
CREATE TABLE "public"."t_department" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"description" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default" NOT NULL,
"company_id" int8
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_department
-- ----------------------------
INSERT INTO "public"."t_department" VALUES ('63', '2017-06-24 11:52:17.916', '2017-06-24 11:52:17.916', '0', '质量与测试部门', '质量部', '62');
INSERT INTO "public"."t_department" VALUES ('65', '2017-06-24 11:52:17.927', '2017-06-24 11:52:17.927', '0', '研发生产部门', '生产部', '62');

-- ----------------------------
-- Table structure for t_post
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_post";
CREATE TABLE "public"."t_post" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"body" text COLLATE "default",
"keywords" varchar(255) COLLATE "default",
"title" varchar(255) COLLATE "default",
"user_id" int8 NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_post
-- ----------------------------

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_role";
CREATE TABLE "public"."t_role" (
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"description" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default" NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_role
-- ----------------------------
INSERT INTO "public"."t_role" VALUES ('58', '2017-06-24 11:52:17.913', '2017-06-24 11:52:17.913', '0', '管理员', 'admin');
INSERT INTO "public"."t_role" VALUES ('59', '2017-06-24 11:52:17.913', '2017-06-24 11:52:17.913', '0', '经理', 'manager');
INSERT INTO "public"."t_role" VALUES ('60', '2017-06-24 11:52:17.914', '2017-06-24 11:52:17.914', '0', '雇员', 'employee');
INSERT INTO "public"."t_role" VALUES ('61', '2017-06-24 11:52:17.914', '2017-06-24 11:52:17.914', '0', '普通用户', 'user');

-- ----------------------------
-- Table structure for t_role_aud
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_role_aud";
CREATE TABLE "public"."t_role_aud" (
"id" int8 NOT NULL,
"rev" int4 NOT NULL,
"revtype" int2,
"description" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default"
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_role_aud
-- ----------------------------
INSERT INTO "public"."t_role_aud" VALUES ('58', '74', '0', '管理员', 'admin');
INSERT INTO "public"."t_role_aud" VALUES ('59', '74', '0', '经理', 'manager');
INSERT INTO "public"."t_role_aud" VALUES ('60', '74', '0', '雇员', 'employee');
INSERT INTO "public"."t_role_aud" VALUES ('61', '74', '0', '普通用户', 'user');

-- ----------------------------
-- Table structure for t_role_authority
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_role_authority";
CREATE TABLE "public"."t_role_authority" (
"role_id" int8 NOT NULL,
"authority_id" int8 NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_role_authority
-- ----------------------------
INSERT INTO "public"."t_role_authority" VALUES ('58', '38');
INSERT INTO "public"."t_role_authority" VALUES ('58', '39');
INSERT INTO "public"."t_role_authority" VALUES ('58', '40');
INSERT INTO "public"."t_role_authority" VALUES ('58', '41');
INSERT INTO "public"."t_role_authority" VALUES ('58', '42');
INSERT INTO "public"."t_role_authority" VALUES ('58', '43');
INSERT INTO "public"."t_role_authority" VALUES ('58', '44');
INSERT INTO "public"."t_role_authority" VALUES ('58', '45');
INSERT INTO "public"."t_role_authority" VALUES ('58', '46');
INSERT INTO "public"."t_role_authority" VALUES ('58', '47');
INSERT INTO "public"."t_role_authority" VALUES ('58', '48');
INSERT INTO "public"."t_role_authority" VALUES ('58', '49');
INSERT INTO "public"."t_role_authority" VALUES ('58', '50');
INSERT INTO "public"."t_role_authority" VALUES ('58', '51');
INSERT INTO "public"."t_role_authority" VALUES ('58', '52');
INSERT INTO "public"."t_role_authority" VALUES ('58', '53');
INSERT INTO "public"."t_role_authority" VALUES ('58', '54');
INSERT INTO "public"."t_role_authority" VALUES ('58', '55');
INSERT INTO "public"."t_role_authority" VALUES ('58', '56');
INSERT INTO "public"."t_role_authority" VALUES ('58', '57');
INSERT INTO "public"."t_role_authority" VALUES ('59', '39');
INSERT INTO "public"."t_role_authority" VALUES ('59', '40');
INSERT INTO "public"."t_role_authority" VALUES ('59', '41');
INSERT INTO "public"."t_role_authority" VALUES ('59', '42');
INSERT INTO "public"."t_role_authority" VALUES ('59', '43');
INSERT INTO "public"."t_role_authority" VALUES ('59', '44');
INSERT INTO "public"."t_role_authority" VALUES ('59', '45');
INSERT INTO "public"."t_role_authority" VALUES ('59', '47');
INSERT INTO "public"."t_role_authority" VALUES ('59', '49');
INSERT INTO "public"."t_role_authority" VALUES ('59', '50');
INSERT INTO "public"."t_role_authority" VALUES ('59', '51');
INSERT INTO "public"."t_role_authority" VALUES ('59', '54');
INSERT INTO "public"."t_role_authority" VALUES ('59', '56');
INSERT INTO "public"."t_role_authority" VALUES ('59', '57');
INSERT INTO "public"."t_role_authority" VALUES ('60', '39');
INSERT INTO "public"."t_role_authority" VALUES ('60', '41');
INSERT INTO "public"."t_role_authority" VALUES ('60', '44');
INSERT INTO "public"."t_role_authority" VALUES ('60', '45');
INSERT INTO "public"."t_role_authority" VALUES ('60', '47');
INSERT INTO "public"."t_role_authority" VALUES ('60', '49');
INSERT INTO "public"."t_role_authority" VALUES ('60', '50');
INSERT INTO "public"."t_role_authority" VALUES ('60', '51');
INSERT INTO "public"."t_role_authority" VALUES ('60', '56');
INSERT INTO "public"."t_role_authority" VALUES ('60', '57');
INSERT INTO "public"."t_role_authority" VALUES ('61', '39');
INSERT INTO "public"."t_role_authority" VALUES ('61', '41');
INSERT INTO "public"."t_role_authority" VALUES ('61', '45');
INSERT INTO "public"."t_role_authority" VALUES ('61', '47');

-- ----------------------------
-- Table structure for t_role_authority_aud
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_role_authority_aud";
CREATE TABLE "public"."t_role_authority_aud" (
"rev" int4 NOT NULL,
"role_id" int8 NOT NULL,
"authority_id" int8 NOT NULL,
"revtype" int2
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_role_authority_aud
-- ----------------------------
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '38', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '39', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '40', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '41', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '42', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '43', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '44', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '45', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '46', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '47', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '48', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '49', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '50', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '51', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '52', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '53', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '54', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '55', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '56', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '58', '57', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '39', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '40', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '41', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '42', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '43', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '44', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '45', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '47', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '49', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '50', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '51', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '54', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '56', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '59', '57', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '39', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '41', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '44', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '45', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '47', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '49', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '50', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '51', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '56', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '60', '57', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '61', '39', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '61', '41', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '61', '45', '0');
INSERT INTO "public"."t_role_authority_aud" VALUES ('74', '61', '47', '0');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_user";
CREATE TABLE "public"."t_user" (
"user_type" varchar(31) COLLATE "default" NOT NULL,
"id" int8 NOT NULL,
"create_date" timestamp(6) NOT NULL,
"modify_date" timestamp(6) NOT NULL,
"version" int4 NOT NULL,
"account_non_expired" bool,
"account_non_locked" bool,
"address" varchar(255) COLLATE "default",
"age" int4,
"birthday" date,
"credentials_non_expired" bool,
"description" varchar(300) COLLATE "default",
"email" varchar(255) COLLATE "default" NOT NULL,
"enabled" bool,
"gender" varchar(255) COLLATE "default",
"icon" oid,
"icon_src" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default",
"password" varchar(255) COLLATE "default" NOT NULL,
"publickey" text COLLATE "default",
"city" varchar(255) COLLATE "default",
"country" varchar(255) COLLATE "default",
"language" varchar(255) COLLATE "default",
"mobile" varchar(255) COLLATE "default",
"province" varchar(255) COLLATE "default",
"telephone" varchar(255) COLLATE "default",
"username" varchar(255) COLLATE "default",
"affiliation" varchar(255) COLLATE "default",
"title" varchar(255) COLLATE "default",
"emp_num" int4,
"post" varchar(255) COLLATE "default",
"salary" float8,
"department_id" int8
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO "public"."t_user" VALUES ('Employee', '64', '2017-06-24 11:52:17.918', '2017-06-24 11:52:17.918', '0', 't', 't', 'XX路25号', '25', '1991-10-24', 't', '普通职员', 'bar@test.com', 't', 'FEMALE', null, 'download/img/icon-head-bar.jpg', 'bar', '$2a$10$GJLpVENNyciMoa3dYgvDtO1sw2quWvW0V.WzLBKUw1Wtrb9tUDMAe', null, '昆明', '中国', 'zh', '130******77', '云南', '67891234', 'bar@test.com', null, null, '2', 'QA人员', '6000', '63');
INSERT INTO "public"."t_user" VALUES ('Employee', '66', '2017-06-24 11:52:17.928', '2017-06-24 11:52:17.928', '0', 't', 't', '北大街XX号', '26', '1990-12-13', 't', '业务管理人员', 'foo@test.com', 't', 'MALE', null, 'download/img/icon-head-foo.jpg', 'foo', '$2a$10$GJLpVENNyciMoa3dYgvDtO1sw2quWvW0V.WzLBKUw1Wtrb9tUDMAe', null, '西安', '中国', 'zh', '139******11', '陕西', '40221199', 'foo@test.com', null, null, '1', '系统分析师', '10000', '65');
INSERT INTO "public"."t_user" VALUES ('Customer', '67', '2017-06-24 11:52:17.929', '2017-06-24 11:52:17.929', '0', 't', 't', '回龙路66号', '35', '1982-02-12', 't', 'developer', 'emailtohl@163.com', 't', 'MALE', null, 'download/img/icon-head-emailtohl.png', 'hl', '$2a$10$GJLpVENNyciMoa3dYgvDtO1sw2quWvW0V.WzLBKUw1Wtrb9tUDMAe', null, '重庆', '中国', 'zh', '187******82', '重庆', '69922113', 'emailtohl@163.com', null, null, null, null, null, null);
INSERT INTO "public"."t_user" VALUES ('Customer', '68', '2017-06-24 11:52:17.93', '2017-06-24 11:52:17.93', '0', 't', 't', '新南路XX号', '21', '1995-11-20', 't', '普通客户', 'baz@test.com', 't', 'FEMALE', null, 'download/img/icon-head-baz.jpg', 'baz', '$2a$10$GJLpVENNyciMoa3dYgvDtO1sw2quWvW0V.WzLBKUw1Wtrb9tUDMAe', null, '成都', '中国', 'zh', '136******87', '四川', '7722134', 'baz@test.com', '客户咨询公司', '客户经理', null, null, null, null);
INSERT INTO "public"."t_user" VALUES ('Customer', '69', '2017-06-24 11:52:17.932', '2017-06-24 11:52:17.932', '0', 't', 't', '竹山路XX号', '24', '1992-07-17', 't', '高级客户', 'qux@test.com', 't', 'FEMALE', null, 'download/img/icon-head-qux.jpg', 'qux', '$2a$10$GJLpVENNyciMoa3dYgvDtO1sw2quWvW0V.WzLBKUw1Wtrb9tUDMAe', null, '南京', '中国', 'zh', '177******05', '江苏', '98241562', 'qux@test.com', '客户咨询公司', '销售经理', null, null, null, null);

-- ----------------------------
-- Table structure for t_user_aud
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_user_aud";
CREATE TABLE "public"."t_user_aud" (
"id" int8 NOT NULL,
"rev" int4 NOT NULL,
"user_type" varchar(31) COLLATE "default" NOT NULL,
"revtype" int2,
"address" varchar(255) COLLATE "default",
"birthday" date,
"description" varchar(255) COLLATE "default",
"email" varchar(255) COLLATE "default",
"enabled" bool,
"gender" varchar(255) COLLATE "default",
"icon_src" varchar(255) COLLATE "default",
"name" varchar(255) COLLATE "default",
"publickey" text COLLATE "default",
"telephone" varchar(255) COLLATE "default",
"username" varchar(255) COLLATE "default",
"emp_num" int4,
"post" varchar(255) COLLATE "default",
"salary" float8,
"affiliation" varchar(255) COLLATE "default",
"title" varchar(255) COLLATE "default"
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_user_aud
-- ----------------------------
INSERT INTO "public"."t_user_aud" VALUES ('64', '74', 'Employee', '0', 'XX路25号', '1991-10-24', '普通职员', 'bar@test.com', 't', 'FEMALE', 'download/img/icon-head-bar.jpg', 'bar', null, '67891234', 'bar@test.com', '2', 'QA人员', '6000', null, null);
INSERT INTO "public"."t_user_aud" VALUES ('66', '74', 'Employee', '0', '北大街XX号', '1990-12-13', '业务管理人员', 'foo@test.com', 't', 'MALE', 'download/img/icon-head-foo.jpg', 'foo', null, '40221199', 'foo@test.com', '1', '系统分析师', '10000', null, null);
INSERT INTO "public"."t_user_aud" VALUES ('67', '74', 'Customer', '0', '回龙路66号', '1982-02-12', 'developer', 'emailtohl@163.com', 't', 'MALE', 'download/img/icon-head-emailtohl.png', 'hl', null, '69922113', 'emailtohl@163.com', null, null, null, null, null);
INSERT INTO "public"."t_user_aud" VALUES ('68', '74', 'Customer', '0', '新南路XX号', '1995-11-20', '普通客户', 'baz@test.com', 't', 'FEMALE', 'download/img/icon-head-baz.jpg', 'baz', null, '7722134', 'baz@test.com', null, null, null, '客户咨询公司', '客户经理');
INSERT INTO "public"."t_user_aud" VALUES ('69', '74', 'Customer', '0', '竹山路XX号', '1992-07-17', '高级客户', 'qux@test.com', 't', 'FEMALE', 'download/img/icon-head-qux.jpg', 'qux', null, '98241562', 'qux@test.com', null, null, null, '客户咨询公司', '销售经理');

-- ----------------------------
-- Table structure for t_user_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_user_role";
CREATE TABLE "public"."t_user_role" (
"user_id" int8 NOT NULL,
"role_id" int8 NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_user_role
-- ----------------------------
INSERT INTO "public"."t_user_role" VALUES ('64', '60');
INSERT INTO "public"."t_user_role" VALUES ('66', '59');
INSERT INTO "public"."t_user_role" VALUES ('67', '58');
INSERT INTO "public"."t_user_role" VALUES ('67', '61');
INSERT INTO "public"."t_user_role" VALUES ('68', '61');
INSERT INTO "public"."t_user_role" VALUES ('69', '61');

-- ----------------------------
-- Table structure for t_user_role_aud
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_user_role_aud";
CREATE TABLE "public"."t_user_role_aud" (
"rev" int4 NOT NULL,
"user_id" int8 NOT NULL,
"role_id" int8 NOT NULL,
"revtype" int2
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Records of t_user_role_aud
-- ----------------------------
INSERT INTO "public"."t_user_role_aud" VALUES ('74', '64', '60', '0');
INSERT INTO "public"."t_user_role_aud" VALUES ('74', '66', '59', '0');
INSERT INTO "public"."t_user_role_aud" VALUES ('74', '67', '58', '0');
INSERT INTO "public"."t_user_role_aud" VALUES ('74', '67', '61', '0');
INSERT INTO "public"."t_user_role_aud" VALUES ('74', '68', '61', '0');
INSERT INTO "public"."t_user_role_aud" VALUES ('74', '69', '61', '0');

-- ----------------------------
-- Alter Sequences Owned By 
-- ----------------------------

-- ----------------------------
-- Primary Key structure for table revinfo
-- ----------------------------
ALTER TABLE "public"."revinfo" ADD PRIMARY KEY ("rev");

-- ----------------------------
-- Primary Key structure for table t_application_form
-- ----------------------------
ALTER TABLE "public"."t_application_form" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_application_handle_history
-- ----------------------------
ALTER TABLE "public"."t_application_handle_history" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_article
-- ----------------------------
ALTER TABLE "public"."t_article" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_article_aud
-- ----------------------------
ALTER TABLE "public"."t_article_aud" ADD PRIMARY KEY ("id", "rev");

-- ----------------------------
-- Primary Key structure for table t_article_comment
-- ----------------------------
ALTER TABLE "public"."t_article_comment" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_article_type
-- ----------------------------
ALTER TABLE "public"."t_article_type" ADD UNIQUE ("name");

-- ----------------------------
-- Primary Key structure for table t_article_type
-- ----------------------------
ALTER TABLE "public"."t_article_type" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_authority
-- ----------------------------
ALTER TABLE "public"."t_authority" ADD UNIQUE ("name");

-- ----------------------------
-- Primary Key structure for table t_authority
-- ----------------------------
ALTER TABLE "public"."t_authority" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_authority_aud
-- ----------------------------
ALTER TABLE "public"."t_authority_aud" ADD PRIMARY KEY ("id", "rev");

-- ----------------------------
-- Primary Key structure for table t_company
-- ----------------------------
ALTER TABLE "public"."t_company" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_department
-- ----------------------------
ALTER TABLE "public"."t_department" ADD UNIQUE ("name");

-- ----------------------------
-- Primary Key structure for table t_department
-- ----------------------------
ALTER TABLE "public"."t_department" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_post
-- ----------------------------
ALTER TABLE "public"."t_post" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_role
-- ----------------------------
ALTER TABLE "public"."t_role" ADD UNIQUE ("name");

-- ----------------------------
-- Primary Key structure for table t_role
-- ----------------------------
ALTER TABLE "public"."t_role" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_role_aud
-- ----------------------------
ALTER TABLE "public"."t_role_aud" ADD PRIMARY KEY ("id", "rev");

-- ----------------------------
-- Primary Key structure for table t_role_authority
-- ----------------------------
ALTER TABLE "public"."t_role_authority" ADD PRIMARY KEY ("role_id", "authority_id");

-- ----------------------------
-- Primary Key structure for table t_role_authority_aud
-- ----------------------------
ALTER TABLE "public"."t_role_authority_aud" ADD PRIMARY KEY ("rev", "role_id", "authority_id");

-- ----------------------------
-- Uniques structure for table t_user
-- ----------------------------
ALTER TABLE "public"."t_user" ADD UNIQUE ("email");
ALTER TABLE "public"."t_user" ADD UNIQUE ("emp_num");

-- ----------------------------
-- Checks structure for table t_user
-- ----------------------------
ALTER TABLE "public"."t_user" ADD CHECK (emp_num >= 1);
ALTER TABLE "public"."t_user" ADD CHECK ((age >= 1) AND (age <= 120));

-- ----------------------------
-- Primary Key structure for table t_user
-- ----------------------------
ALTER TABLE "public"."t_user" ADD PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_user_aud
-- ----------------------------
ALTER TABLE "public"."t_user_aud" ADD PRIMARY KEY ("id", "rev");

-- ----------------------------
-- Primary Key structure for table t_user_role
-- ----------------------------
ALTER TABLE "public"."t_user_role" ADD PRIMARY KEY ("user_id", "role_id");

-- ----------------------------
-- Primary Key structure for table t_user_role_aud
-- ----------------------------
ALTER TABLE "public"."t_user_role_aud" ADD PRIMARY KEY ("rev", "user_id", "role_id");

-- ----------------------------
-- Foreign Key structure for table "public"."t_application_form"
-- ----------------------------
ALTER TABLE "public"."t_application_form" ADD FOREIGN KEY ("user_applicant_id") REFERENCES "public"."t_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."t_application_form" ADD FOREIGN KEY ("user_handler_id") REFERENCES "public"."t_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_application_handle_history"
-- ----------------------------
ALTER TABLE "public"."t_application_handle_history" ADD FOREIGN KEY ("handler_id") REFERENCES "public"."t_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."t_application_handle_history" ADD FOREIGN KEY ("application_form_id") REFERENCES "public"."t_application_form" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_article"
-- ----------------------------
ALTER TABLE "public"."t_article" ADD FOREIGN KEY ("article_type_id") REFERENCES "public"."t_article_type" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."t_article" ADD FOREIGN KEY ("author_id") REFERENCES "public"."t_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_article_aud"
-- ----------------------------
ALTER TABLE "public"."t_article_aud" ADD FOREIGN KEY ("rev") REFERENCES "public"."revinfo" ("rev") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_article_comment"
-- ----------------------------
ALTER TABLE "public"."t_article_comment" ADD FOREIGN KEY ("article_id") REFERENCES "public"."t_article" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_article_type"
-- ----------------------------
ALTER TABLE "public"."t_article_type" ADD FOREIGN KEY ("parent_type") REFERENCES "public"."t_article_type" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_authority_aud"
-- ----------------------------
ALTER TABLE "public"."t_authority_aud" ADD FOREIGN KEY ("rev") REFERENCES "public"."revinfo" ("rev") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_department"
-- ----------------------------
ALTER TABLE "public"."t_department" ADD FOREIGN KEY ("company_id") REFERENCES "public"."t_company" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_post"
-- ----------------------------
ALTER TABLE "public"."t_post" ADD FOREIGN KEY ("user_id") REFERENCES "public"."t_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_role_aud"
-- ----------------------------
ALTER TABLE "public"."t_role_aud" ADD FOREIGN KEY ("rev") REFERENCES "public"."revinfo" ("rev") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_role_authority"
-- ----------------------------
ALTER TABLE "public"."t_role_authority" ADD FOREIGN KEY ("role_id") REFERENCES "public"."t_role" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."t_role_authority" ADD FOREIGN KEY ("authority_id") REFERENCES "public"."t_authority" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_role_authority_aud"
-- ----------------------------
ALTER TABLE "public"."t_role_authority_aud" ADD FOREIGN KEY ("rev") REFERENCES "public"."revinfo" ("rev") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_user"
-- ----------------------------
ALTER TABLE "public"."t_user" ADD FOREIGN KEY ("department_id") REFERENCES "public"."t_department" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_user_aud"
-- ----------------------------
ALTER TABLE "public"."t_user_aud" ADD FOREIGN KEY ("rev") REFERENCES "public"."revinfo" ("rev") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_user_role"
-- ----------------------------
ALTER TABLE "public"."t_user_role" ADD FOREIGN KEY ("role_id") REFERENCES "public"."t_role" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."t_user_role" ADD FOREIGN KEY ("user_id") REFERENCES "public"."t_user" ("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Key structure for table "public"."t_user_role_aud"
-- ----------------------------
ALTER TABLE "public"."t_user_role_aud" ADD FOREIGN KEY ("rev") REFERENCES "public"."revinfo" ("rev") ON DELETE NO ACTION ON UPDATE NO ACTION;
