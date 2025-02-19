<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of content.jsp(xxx.jsp)   -->
<div class="col-sm-8">
	<h2>회원 가입</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>

	<form action="/user/sign-up" method="post" enctype="multipart/form-data">
		<div class="form-group">
			<label for="username">username:</label> <input type="text" class="form-control" placeholder="Enter username" id="username" name="username" value="야스오1">
		</div>
		<div class="form-group">
			<label for="pwd">Password:</label> <input type="password" class="form-control" placeholder="Enter password" id="pwd" name="password" value="asd123">
		</div>
		<div class="form-group">
			<label for="fullname">fullname:</label> <input type="text" class="form-control" placeholder="Enter fullname" id="fullname" name="fullname" value="바람검객">
		</div>
		<div class="custom-file">
			<input type="file" class="custom-file-input" id="customFile" name="customFile">
			<label class="custom-file-label"  for="customFile">파일 선택 🔎</label>
		</div>
		<br>
		<div class="d-flex justify-content-end">
			<button type="submit" class="btn btn-primary mt-md-4">회원가입</button>
		</div>
		<div>
			<a href="https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=21625653d6c77548fa54d359f8707123&redirect_uri=http://localhost:8080/user/kakao">
			 <img alt="소셜로그인이미지" src="/images/kakao_login_small.png">
			</a>
		</div>
	</form>


</div>
<!-- end of col-sm-8  -->
</div>
</div>
<!-- end of content.jsp(xxx.jsp)   -->

<script>
// Add the following code if you want the name of the file appear on select
$(".custom-file-input").on("change", function() {
  console.log($(this).val());	
  let fileName = $(this).val().split("\\").pop();
  $(this).siblings(".custom-file-label").addClass("selected").html(fileName);
});
</script>

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>



