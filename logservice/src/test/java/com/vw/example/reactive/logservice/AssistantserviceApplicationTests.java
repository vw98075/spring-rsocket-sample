package com.vw.example.reactive.logservice;

import com.vw.example.reactive.logservice.repository.WordSearchLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class AssistantserviceApplicationTests {

	@Autowired
	private WordSearchLogRepository repository;

	@Test
	void contextLoads() {
	}

	@Test
	public void whenDeleteAll_then0IsExpected() {
		repository.deleteAll()
				.as(StepVerifier::create)
				.expectNextCount(0)
				.verifyComplete();
	}

	@Test
	public void youDo(){


//		int [] inArr = {2,4, 6,7, 9, 11, 13, 16, 17, 18, 20, 22 };
//
//		// divide  upbond + duration * n
//
//		var upbond = inArr[0] + duration;
//		var count =0;
//		List<Integer> result = new ArrayList();
//		for(int a: inArr){
//			if(a <= upbond){
//				count++;
//			}else{
//				result.add(count);
//				count = 1;
//				upbond += duration;
//			}
//		}
//		result.add(count);
//		System.out.println(Arrays.toString(result.toArray()));

	}
}
