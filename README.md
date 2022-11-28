## CRUD 게시판 만들기

공부한 내용을 이것 저것 적용해보는 곳     
  
* 회원 가입, 쿠키와 세션을 이용한 로그인 로그아웃 처리  
* 회원관리, 게시글 관리, 파일 업로드, 파일 다운로드   
* 기능 별로 Dto를 나눔
* 스프링 인터셉터를 사용하여 로그인 창으로 보내고 로그인 후 redirect
* @Valid로 validation 적용하고 bindingResult.hasError를 사용하여 필드 오류 메세지 처리  
bindingResult.reject와 errors.properties를 사용하여 글로벌 오류 메세지 처리       
* messages.properties를 사용하여 공통 메세지 처리  
* 잘못된 URL을 요청하거나 서버 문제일 때 status 4xx, 5xx 오류 페이지 보여주기

## API 설계  

|기능|URL|Method|                      
|---|---|---|                                               
|회원 가입 폼| /signup| Get|            
|회원 가입 | /signup| Post| 
|로그인 폼| /login| Get|
|로그인|/login|Post|
|로그아웃|/logout|Get|      
|홈 화면|/home|Get|
|회원 삭제 폼|/deleteMember|Get|
|회원 삭제|/deleteMember|Post|
|회원 수정 폼|/updateMember|Get|
|회원 수정|/updateMember|Post|
|게시글 등록 폼|/home/registration|Get|
|게시글 등록|/home/registration|Post|
|전체 게시글 보기|/home/postList|Get|
|검색 게시글 찾기|/home/findPosts|Post|
|파일 업로드 폼|/upload|Get|
|파일 업로드|/upload|Post|
|파일 다운로드 폼|/download|Get|
|파일 다운로드|/download|Post|

## 개발환경 

Spring Framework   

Project : Gradle project  
Language : Java 11                
Spring boot : 2.7.5     
Dependencies : Spring Web, Spring Data JPA, Lombok, Thymeleaf, H2 Database, Validation

## trouble shooting

* 로그인을 할 때 @PostMapping에서 @ModelAttribute로 받을 때 Member가 아니라 id가 없는 MemberDto로 먼저 받아야 한다.   
ModelAttribute가 들어온 값에 대해 setter를 이용해서 객체를 만들어주는데 id가 있는 Member로 받으려고 해서 문제 발생
* Member와 Post를 연관관계 지어줄 때 Many 쪽인 Post에 setMember로 연결했다.   
그러므로 postService로 post를 저장하기 전에 먼저 setMember를 사용해야 연관관계가 완성된다. 
* Member에는 username으로 선언했는데 MemberRepository에 findByName() 메소드를 만들었다. findByUsername()으로 만들어야 JpaRepository를 상속받아 정상적으로 작동한다.
* Test코드를 작성할 때 @SpringBootTest를 해야 하는데 @SpringBootApplication이라고 해서 NPE가 계속 나왔다.   
또한 이때는 DB를 연결하고 실행해야 한다.   
* 회원 수정 기능을 만들 때 member에 @Setter없이 하기 위해서 Member 클래스 안에 updateMember를 만들어주었다. 
```
public Member updateMember(String username, String password) {
    this.username = username;
    this.password = password;
    return this;
}
```
또한 회원 수정은 JPA에서 따로 지원하는 기능은 없고 저장하고자 하는 Entity의 PK 값이 있으면 업데이트고 없으면 저장할 수 있다.   
* 쿼리 파라미터로 {id} 회원을 로그인 상태로 유지하는 방법이 아닌 세션을 이용해서 유지되도록 하였다. 
```
@SessionAttribute(name = "loginMember", required = false) Member loginMember
```
이때 loginMember를 그대로 사용하면 영속성 컨텍스트가 없어 post데이터를 가져올 수 없었다. 이때는 MemberService를 이용해서  
```
Member member = memberService.findByUsername(loginMember.getUsername()).get();
```
member를 다시 찾아줬더니 해결되었다.
* 전체 게시글을 보는 것은 로그인 후에 가능하다. 따라서 전체 게시글 보기를 클릭하면 다음 경로로 redirect된다.   
/login?redirectURL=/home/postList
다시 로그인을 하면 redirect된 경로로 접속을 하려고 했는데 계속 홈 화면으로 돌아갔다.   
문제는 login.html에서 th:action="@{/login}" 으로 고정되어 있었기 때문이다.  
th:action으로 바꿔주면 현재 URL과 같은 URL로 요청을 보낼 수 있다. 이때는 쿼리 파라미터 부분이 있기 때문에  
@RequestParam을 이용해서 가져올 수 있다.
