package com.github.emailtohl.integration.web.config;

import java.io.Serializable;

import com.github.emailtohl.integration.core.config.CorePresetData;
import com.github.emailtohl.integration.core.role.Role;
import com.github.emailtohl.integration.core.role.RoleType;
import com.github.emailtohl.integration.core.user.entities.Department;
import com.github.emailtohl.integration.core.user.entities.Employee;
import com.github.emailtohl.integration.web.service.cms.entities.Type;

/**
 * 本web模块的预置数据
 * 对于public final域的，是系统中不会变的数据，而private域中的，则只在系统初始化时执行一遍，以后可以被用户改变
 * 
 * @author HeLei
 */
public class WebPresetData implements Serializable {
	private static final long serialVersionUID = 8938868828524481149L;
	
	public final Type unclassified = new Type("unclassified", "未分类", null);
	
	private Department market = new Department("market", "市场部", null);
	private Department financial = new Department("financial", "财务部", null);
	private Department business = new Department("business", "业务部", null);
	private Department humanResource = new Department("human resource", "人力资源部", null);
	private Department back = new Department("back", "后勤部", null);
	
	private Role generalManager = new Role("generalManager", RoleType.EMPLOYEE, "总经理");
	private Role deptLeader = new Role("deptLeader", RoleType.EMPLOYEE, "部门经理");
	private Role hr = new Role("hr", RoleType.EMPLOYEE, "人事经理");
	private Role treasurer = new Role("treasurer", RoleType.EMPLOYEE, "财务人员");
	private Role cashier = new Role("cashier", RoleType.EMPLOYEE, "出纳员");
	private Role supportCrew = new Role("supportCrew", RoleType.EMPLOYEE, "后勤人员");
	
	private Employee bill = new Employee();
	private Employee jenny = new Employee();
	private Employee eric = new Employee();
	private Employee tom = new Employee();
	private Employee kermit = new Employee();
	private Employee amy = new Employee();
	private Employee andy = new Employee();
	private Employee tony = new Employee();
	private Employee lily = new Employee();
	private Employee thomas = new Employee();
	
	public WebPresetData() {
		CorePresetData cpd = new CorePresetData();
		
		bill.setName("Bill");
		bill.setNickname("Zheng");
		bill.setEmail("bill@localhost");
		bill.setPassword("123456");
		bill.getRoles().add(generalManager);
		generalManager.getUsers().add(bill);
		
		jenny.setName("Jenny");
		jenny.setNickname("Luo");
		jenny.setEmail("jenny@localhost");
		jenny.setPassword("123456");
		jenny.setDepartment(humanResource);
		humanResource.getEmployees().add(jenny);
		jenny.getRoles().add(hr);
		hr.getUsers().add(jenny);
		
		eric.setName("Eric");
		eric.setNickname("Li");
		eric.setEmail("eric@localhost");
		eric.setPassword("123456");
		eric.setDepartment(market);
		market.getEmployees().add(eric);
		eric.getRoles().add(cpd.role_staff);
		cpd.role_staff.getUsers().add(eric);
		
		tom.setName("Tom");
		tom.setNickname("Wang");
		tom.setEmail("tom@localhost");
		tom.setPassword("123456");
		tom.setDepartment(market);
		market.getEmployees().add(tom);
		tom.getRoles().add(cpd.role_staff);
		cpd.role_staff.getUsers().add(tom);
		
		kermit.setName("Kermit");
		kermit.setNickname("Miao");
		kermit.setEmail("kermit@localhost");
		kermit.setPassword("123456");
		kermit.setDepartment(market);
		market.getEmployees().add(kermit);
		kermit.getRoles().add(deptLeader);
		deptLeader.getUsers().add(kermit);
		
		amy.setName("Amy");
		amy.setNickname("Zhang");
		amy.setEmail("amy@localhost");
		amy.setPassword("123456");
		amy.setDepartment(business);
		business.getEmployees().add(amy);
		amy.getRoles().add(cpd.role_staff);
		cpd.role_staff.getUsers().add(amy);
		
		andy.setName("Andy");
		andy.setNickname("Zhao");
		andy.setEmail("andy@localhost");
		andy.setPassword("123456");
		andy.setDepartment(business);
		business.getEmployees().add(andy);
		andy.getRoles().add(deptLeader);
		deptLeader.getUsers().add(andy);
		
		tony.setName("Tony");
		tony.setNickname("Zhang");
		tony.setEmail("tony@localhost");
		tony.setPassword("123456");
		tony.setDepartment(financial);
		financial.getEmployees().add(tony);
		tony.getRoles().add(treasurer);
		treasurer.getUsers().add(tony);
		
		lily.setName("Lily");
		lily.setNickname("Song");
		lily.setEmail("lily@localhost");
		lily.setPassword("123456");
		lily.setDepartment(financial);
		financial.getEmployees().add(lily);
		lily.getRoles().add(cashier);
		cashier.getUsers().add(lily);
		
		thomas.setName("Thomas");
		thomas.setNickname("Wang");
		thomas.setEmail("thomas@localhost");
		thomas.setPassword("123456");
		thomas.setDepartment(back);
		back.getEmployees().add(thomas);
		thomas.getRoles().add(supportCrew);
		supportCrew.getUsers().add(thomas);
	}

	public Department getMarket() {
		return market;
	}
	public void setMarket(Department market) {
		this.market = market;
	}

	public Department getFinancial() {
		return financial;
	}
	public void setFinancial(Department financial) {
		this.financial = financial;
	}

	public Department getBusiness() {
		return business;
	}
	public void setBusiness(Department business) {
		this.business = business;
	}

	public Department getHumanResource() {
		return humanResource;
	}
	public void setHumanResource(Department resource) {
		this.humanResource = resource;
	}

	public Department getBack() {
		return back;
	}
	public void setBack(Department back) {
		this.back = back;
	}

	public Role getGeneralManager() {
		return generalManager;
	}
	public void setGeneralManager(Role generalManager) {
		this.generalManager = generalManager;
	}

	public Role getDeptLeader() {
		return deptLeader;
	}
	public void setDeptLeader(Role deptLeader) {
		this.deptLeader = deptLeader;
	}

	public Role getHr() {
		return hr;
	}
	public void setHr(Role hr) {
		this.hr = hr;
	}

	public Role getTreasurer() {
		return treasurer;
	}
	public void setTreasurer(Role treasurer) {
		this.treasurer = treasurer;
	}

	public Role getCashier() {
		return cashier;
	}
	public void setCashier(Role cashier) {
		this.cashier = cashier;
	}

	public Role getSupportCrew() {
		return supportCrew;
	}
	public void setSupportCrew(Role supportCrew) {
		this.supportCrew = supportCrew;
	}

	public Employee getBill() {
		return bill;
	}
	public void setBill(Employee bill) {
		this.bill = bill;
	}

	public Employee getJenny() {
		return jenny;
	}
	public void setJenny(Employee jenny) {
		this.jenny = jenny;
	}

	public Employee getEric() {
		return eric;
	}
	public void setEric(Employee eric) {
		this.eric = eric;
	}

	public Employee getTom() {
		return tom;
	}
	public void setTom(Employee tom) {
		this.tom = tom;
	}

	public Employee getKermit() {
		return kermit;
	}
	public void setKermit(Employee kermit) {
		this.kermit = kermit;
	}

	public Employee getAmy() {
		return amy;
	}
	public void setAmy(Employee amy) {
		this.amy = amy;
	}

	public Employee getAndy() {
		return andy;
	}
	public void setAndy(Employee andy) {
		this.andy = andy;
	}

	public Employee getTony() {
		return tony;
	}
	public void setTony(Employee tony) {
		this.tony = tony;
	}

	public Employee getLily() {
		return lily;
	}
	public void setLily(Employee lily) {
		this.lily = lily;
	}

	public Employee getThomas() {
		return thomas;
	}
	public void setThomas(Employee thomas) {
		this.thomas = thomas;
	}
}
