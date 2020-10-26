## 추천데이터 배치 프로그램

	log기반 추천데이터를 가지고 redis에 데이터 값을 넣는다.
	기존 cfengine은 기본java로 되어있어서 springboot로 재구성함.

### 배치시간

		crontab -l을 해보면 새벽1시에 rmsubmit이 설정되어있을 것을 확인할 수 있다.
		0 1 * * * . $HOME/.bashrc; $DMP_HOME/bin/rmsubmit



#### 기존cfengine 대비 변경점

	********* 기존 *********
	기존 cf엔진은 두개의 void main을 포함하는 App.java가 존재하였으며
	com.enliple.recom3.submit.App.java
	com.enliple.recom3.worker.App.java 
	
	submit App
	- was기반 로그파일을 광고주별 파일로 나누는 작업
	- job_list, job_list_conv, job_list_ubcf 테이블에 작업할 광고주리스트를 삽입
	- ssh로 rmworker쉘을 실행
	worker App
	- submit프로그램에서 나눈 로그파일을 추천프로그램 배치에 활용하여 추천데이터를 redis에 넣음
	
	********* springboot 리뉴얼 *********
	1. CommandLineRunner를 구현한 AppRunner한개가 존재
	rmsubmit쉘을 실행할때 파라미터를 넘기게 되면
	기존 submit을 실행하고 바로 rmworker을 실행하는 구조이다.
	ssh로 rmworker쉘을 실행하는 부분은 삭제되었다.
	rmworker만 실행하고 싶을때는 쉘에서 기존처럼 rmworker를 실행하면 된다.
	
	2. 큐 및 멀티쓰레드풀
	멀티쓰레드풀 작동은 UBCF만 존재하며 큐는 @Async로 파일을 미리 읽어서
	큐에 대기하는 형태로 속도개선을 하였다.
	
	3. 로깅
	기존은
	logging.config=/home/users/rpapp/home/data/rmsubmit_logback.xml
	logging.config=/home/users/rpapp/home/data/rmworker_logback.xml
	에서 로그를 관리하였지만 
	springboot 리뉴얼에서는 application-prod.properties 파일에서 따로 설정하였다.
	logging.config=classpath:log4j_prod.xml
	
	4. 프로퍼티 
	기존은 home/data/rmsubmit.properties 에서 프로퍼티를 관리하였지만 
	springboot 리뉴얼에서는  application-prod.properties 프로퍼티 파일을 내포하고있으며
	-Dspring.profiles.active=prod 를 실행옵션을 두어 prod프로퍼티를 읽는다.


### 기타 참고사항

	1. 메모리사용량
	- UBCF와 IBCF의 작동방식이 약간 다른데 IBCF는 기존처럼 같이 본 상품 카운트가 메모리에 적재되서 코사인유사도를 
	계산하는 형태이지만 UBCF는 메모리 적재부분을 제거하고 멀티쓰레드 CPU계산 방식으로 바꿔서 속도와 메모리 사용량 둘다 개선하였다. 
	그래도 기존처럼 IBCF는 128기가를 사용한다.
	
	2. UBCF 제외 광고주
	- default.ubcf.pass.auid=광고주아이디:pc구분
	
	3. rmsubmit은 리뉴얼 하지 못했다.
	- 기존DBCP사용하던 부분을 JPA로 바꾼정도만 하였다. (권대리 화이팅)
		
	4. 이제 rmsubmit하나만 호출한다.
	- 위에서 [기존cf엔진 대비 변경점]에서 언급은 하였지만 ssh로 rmworker쉘 실행부분을 삭제하였다.
		Springboot 실행 쉘스크립트인 rmsubmit과 rmworker실행의 차이점은 파라미터존재 유무이다.
			if(args.length>0) {
				startRmsubmit(args);
			}
	
	5. 기존 프로그램 및 기존 쉘 백업
	- 기존 쉘 백업 및 신규 쉘
	기존 rmsubmit => rmsubmit_old 로 백업
	신규 rmsubmit => springboot용
	기존 rmworker => rmworker_old 로 백업
	신규 rmworker => springboot용
	
	-기존 프로그램 및 신규 리뉴얼 프로그램
	기존 recom.worker-2.5.0-SNAPSHOT.jar 그대로 둠
	신규 recom.worker.springboot-2.6.0.jar 신규추가
	