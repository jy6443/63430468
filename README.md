# 63430468

## 로그인, 회원가입
* Spring Security + Jwt 로 access token과 refresh token으로 사용자의 인증과 허가를 처리했습니다.
* 회원가입에는 사용자의 이메일을 받아 smtp.gmail.com 로 이메일 인증을 통과하고 비밀번호는 알파벳 + 숫자 + 특수기호로 이루어진 8자리 이상의 비밀번호만 허락하는 제약조건을 만들었습니다.
   그리고 중복되지 않는 닉네임으로 게시판에서 사용자의 닉네임으로 보여줄 수 있도록 처리했습니다.
* 로그인 처리가 되면 DB에는 인코딩된 비밀번호와 이메일이 저장되고, 클라이언트에는 access token을 헤더에 담아 보내고 쿠키에는 Http Only로 refresh token을 저장합니다.

### Spring Security + Jwt필터
1. 헤더를 통해 access token을 받은 클라이언트는 로컬 스토리지에 access token을 저장하고 axios 인터셉트로 request에 항상 access token을 담아 서버로 보내고 401 권한 미인증 에러가 뜰 때에 서버로부터 refresh token을 통한 access token 재발급 요청을 보냅니다.
2. 재발급한 access token을 다시 클라이언트는 요청을 보낼때 사용하고 refresh token은 재발급에 쓰였기 때문에 쿠키에서 삭제합니다.
3. 만약 재발급에 쓸 refresh token도 없다면 바로 /LOGIN으로 이동시킵니다.
4. 이때 모든 경로는 permitAll을 하되, 글 작성 엔드포인트에는 jwt필터를 걸어서 유저 인증 이후에 글 작성을 진행하도록 만듭니다.
