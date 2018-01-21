<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>AdminLTE 2 | Log in</title>
  <!-- Tell the browser to be responsive to screen width -->
  <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
  <!-- Bootstrap 3.3.7 -->
  <link rel="stylesheet" href="lib/bootstrap/css/bootstrap.min.css">
  <!-- Font Awesome -->
  <link rel="stylesheet" href="lib/assets/css/font-awesome.min.css">
  <!-- Ionicons -->
  <link rel="stylesheet" href="lib/assets/css/ionicons.min.css">
  <!-- Theme style -->
  <link rel="stylesheet" href="lib/adminLTE/css/AdminLTE.min.css">
  <!-- iCheck -->
  <link rel="stylesheet" href="lib/iCheck/square/blue.css">

  <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
  <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
  <!--[if lt IE 9]>
  <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
  <![endif]-->

  <!-- Google Font -->
  <link rel="stylesheet" href="lib/assets/fonts/SourceSansPro.css">
  <link rel="stylesheet" href="lib/select2/select2.min.css">
</head>
<body class="hold-transition login-page">
<div class="login-box">
  <div class="login-logo">
    <a href="../../index2.html"><b>Admin</b>LTE</a>
  </div>
  <!-- /.login-logo -->
  <div class="login-box-body">
    <p class="login-box-msg">Sign in to start your session</p>
     <c:if test="${param.containsKey('error')}">
	     <div class="callout callout-danger">
	       <h4>Warning!</h4>
	       <p>Login failed. Please try again.</p>
	     </div>
         <!-- <b style="color: red;">Login failed. Please try again.</b><br /><br /> -->
     </c:if>
     <c:if test="${param.containsKey('loggedOut')}">
       <div class="callout callout-info">
          <h4>Tip!</h4>
          <p>You are now logged out.</p>
        </div>
        <!-- <b style="color: red;">You are now logged out.</b><br /><br /> -->
     </c:if>
     <c:if test="${param.containsKey('maxSessions')}">
	     <div class="callout callout-danger">
	       <h4>Warning!</h4>
	       <p>The login you have already done.</p>
	     </div>
         <!-- <b style="color: red;">Login failed. Please try again.</b><br /><br /> -->
     </c:if>
     <p id="publicKey" style="display:none">${publicKey}</p>
    <form action="${pageContext.request.contextPath}/login" method="post">
      <div class="form-group has-feedback">
        <input type="text" class="form-control" name="email" placeholder="your email">
        <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
      </div>
      <div class="form-group has-feedback">
        <input type="password" class="form-control" name="password" placeholder="password">
        <span class="glyphicon glyphicon-lock form-control-feedback"></span>
      </div>
      <!-- 启用CSRF功能时，spring security将会把token写入此表单中 -->
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <div class="row">
        <div class="col-xs-8">
          <div class="checkbox icheck">
            <label>
              <input type="checkbox" name="remember-me"> Remember Me
            </label>
          </div>
        </div>
        <!-- /.col -->
        <div class="col-xs-4">
          <button type="submit" class="btn btn-primary btn-block btn-flat">Sign In</button>
        </div>
        <!-- /.col -->
      </div>
    </form>

    <div class="social-auth-links text-center">
      <p>- OR -</p>
      <a href="#" class="btn btn-block btn-social btn-facebook btn-flat"><i class="fa fa-facebook"></i> Sign in using
        Facebook</a>
      <a href="#" class="btn btn-block btn-social btn-google btn-flat"><i class="fa fa-google-plus"></i> Sign in using
        Google+</a>
    </div>
    <!-- /.social-auth-links -->

    <a id="forgot" href="javascript:void(0)">I forgot my password</a><br>
    <a href="register" class="text-center">Register a new membership</a>

  </div>
  <!-- /.login-box-body -->
</div>
<!-- /.login-box -->

<div class="modal modal-danger">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">×</span></button>
        <h4 class="modal-title">tip</h4>
      </div>
      <div class="modal-body">
        <p id="content"></p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline pull-left" data-dismiss="modal">Close</button>
      </div>
    </div>
    <!-- /.modal-content -->
  </div>
  <!-- /.modal-dialog -->
</div>

<!-- jquery-3.2.1.min.js -->
<script src="lib/jquery/jquery-3.2.1.min.js"></script>
<!-- Bootstrap 3.3.7 -->
<script src="lib/bootstrap/js/bootstrap.min.js"></script>
<!-- iCheck -->
<script src="lib/iCheck/icheck.min.js"></script>
<!-- 我的RSA加密，保护用户登录密码  -->
<script src="lib/cryptico-with-base64-myrsa-myaes.min.js"></script>
<script>
  $(function () {
	$('input').iCheck({
	  checkboxClass: 'icheckbox_square-blue',
	  radioClass: 'iradio_square-blue',
	  increaseArea: '20%' // optional
	});
    
	function tip(content) {
	 var div = $('div.modal');
	 div.find('#content').text(content);
	 div.modal('show'); // initializes and invokes show immediately
	}
	
	$('a#forgot').on('click', function() {
	 var email, _csrf, p;
	 p = /^[a-z0-9`!#$%^&*'{}?/+=|_~-]+(\.[a-z0-9`!#$%^&*'{}?/+=|_~-]+)*@([a-z0-9]([a-z0-9-]*[a-z0-9])?)+(\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)*$/
	 email = $('input[name="email"]').val();
	 _csrf = $('input[name="_csrf"]').val();
	 if (!email || email.match(p) == null) {
	  tip('Please fill in your email address correctly');
	  return false;
	 }
	 $.post('forgetPassword', {
	  email : email,
	  _csrf : _csrf
	 });
	 tip('Please check the E-mail and reset the password');
	});
    /* 
	$('form').on('submit', function(e) {
		e.preventDefault();
		$.ajax('login', {
			type : "POST",
			xhrFields : {
				withCredentials : true
			},
			crossDomain : true,
			success : function(data, status, xhr) {
				window.location('/');
			},
			data : $('form').serialize()
		})
	});
	 */
	 
	 $('form').on('submit', function(e) {
		 var publicKey = $('#publicKey').text();
		 var password = $('input[name="password"]').val();
		 var encryptPassword = myrsa.encrypt(password, publicKey);
		 if (encryptPassword)
		 	$('input[name="password"]').val(encryptPassword);
		 
		 return true;
	 });
	 
  });
  
</script>
</body>
</html>