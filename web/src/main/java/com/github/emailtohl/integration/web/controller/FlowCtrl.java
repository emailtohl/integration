package com.github.emailtohl.integration.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.emailtohl.integration.core.ExecResult;
import com.github.emailtohl.integration.web.service.flow.CommentInfo;
import com.github.emailtohl.integration.web.service.flow.FlowData;
import com.github.emailtohl.integration.web.service.flow.FlowService;
import com.github.emailtohl.lib.jpa.EntityBase;
import com.github.emailtohl.lib.jpa.Paging;
/**
 * 流程控制器
 * @author HeLei
 */
@RestController
@RequestMapping(value = "flow", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FlowCtrl {
	protected static final Logger logger = LogManager.getLogger();
	@Inject
	FlowService flowService;
	/**
	 * 保存请假实体并启动流程
	 * 表单需要参数：
	 * flowType：流程类型
	 * content： 申请内容
	 * 已登录，可以获取到用户id
	 * 
	 * @return 启动的流程实例
	 */
	@RequestMapping(value = "startWorkflow", method = POST)
	@ResponseStatus(HttpStatus.CREATED)
	public FlowData startWorkflow(@RequestBody FlowData form) {
		return flowService.startWorkflow(form);
	}

	/**
	 * 查询当前用户的任务
	 * 需要参数：
	 * 已登录，可以获取到用户id
	 * @return
	 */
	@RequestMapping(value = "todoTasks", method = GET)
	public List<FlowData> findTodoTasks() {
		return flowService.findTodoTasks();
	}

	/**
	 * 签收任务
	 * 表单需要参数：
	 * taskId：任务id
	 * 已登录，可以获取到用户id
	 * @param taskId
	 */
	@RequestMapping(value = "claim/{taskId}", method = POST)
	public ExecResult claim(@PathVariable("taskId") String taskId) {
		return flowService.claim(taskId);
	}

	/**
	 * 审核任务
	 * 表单需要参数：
	 * id：流程单的id或者是流程实例id（processInstanceId）
	 * taskId： 任务id
	 * 已登录，可以获取到用户id
	 * assignee：任务签收人的id
	 * checkApproved：审核是否通过
	 * checkComment： 审核意见可选
	 * @return 执行是否成功
	 */
	@RequestMapping(value = "check", method = POST)
	public ExecResult check(@RequestBody FlowData form) {
		return flowService.check(form);
	}

	/**
	 * 重新申请
	 * 表单modifyApply必填：若为true则重新申请，若为false则结束流程
	 * content：若重新申请，则content需填写
	 * @return 执行是否成功
	 */
	@RequestMapping(value = "reApply", method = POST)
	public ExecResult reApply(@RequestBody FlowData form) {
		return flowService.reApply(form);
	}

	/**
	 * 查询审批时的批注信息
	 * @param processInstanceId
	 * @return
	 */
	@RequestMapping(value = "commentInfo/{processInstanceId}", method = GET)
	public List<CommentInfo> getCommentInfo(@PathVariable("processInstanceId") String processInstanceId) {
		return flowService.getCommentInfo(processInstanceId);
	}

	/**
	 * 查询流程数据
	 * 
	 * @param params
	 * @param pageable
	 * @return
	 */
	@RequestMapping(value = "page", method = GET)
	public Paging<FlowData> query(FlowData params, @PageableDefault(page = 0, size = 10, sort = { EntityBase.ID_PROPERTY_NAME,
			EntityBase.MODIFY_TIME_PROPERTY_NAME }, direction = Direction.DESC) Pageable pageable) {
		return flowService.query(params, pageable);
	}

	@RequestMapping(value = "list", method = GET)
	public List<FlowData> query(FlowData params) {
		return flowService.query(params);
	}

	/**
	 * 通过流程数据id查询流程数据
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "{id}", method = GET)
	public FlowData findByFlowDataId(@PathVariable("id") Long id) {
		return flowService.findByFlowDataId(id);
	}

	/**
	 * 通过流程实例id查询流程数据
	 * 
	 * @param processInstanceId
	 * @return
	 */
	@RequestMapping(value = "byProcessInstanceId/{processInstanceId}", method = GET)
	public FlowData findByProcessInstanceId(@PathVariable("processInstanceId") String processInstanceId) {
		return flowService.findByProcessInstanceId(processInstanceId);
	}

	/**
	 * 通过流程单号查询流程数据
	 * 
	 * @param processInstanceId
	 * @return
	 */
	@RequestMapping(value = "byFlowNum/{flowNum}", method = GET)
	public FlowData findByFlowNum(@PathVariable("flowNum") String flowNum) {
		return flowService.findByFlowNum(flowNum);
	}
}
