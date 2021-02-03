import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.HashMap;
import java.util.Map;

import static org.activiti.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestProcessTest {

	@Configuration
	public static class MessagesServiceTestConfiguration {

		@Bean
		public ActivitiRule activitiRule() {
			return new ActivitiRule(processEngine());
		}

		@Bean
		public ProcessEngine processEngine() {
			return new StandaloneProcessEngineConfiguration()
					.setJdbcUrl("jdbc:h2:mem:activiti;DB_CLOSE_DELAY=1000")
					.setJdbcUsername("sa")
					.setJdbcPassword("")
					.setJdbcDriver("org.h2.Driver")
					.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_TRUE)
					.buildProcessEngine();
		}
	}

	@Rule
	@Autowired
	public ActivitiRule activitiRule;

	@Test
	@Deployment(resources = "META-INF/bpmn/TestProcess.bpmn")
	public void smokeTestProcess() {

		ProcessEngine processEngine = activitiRule.getProcessEngine();
		Map<String, Object> variables = new HashMap<>();

		/** Start the process */

		ProcessInstance processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey("testProcess", "myTest", variables);

		Task currentTask = currentTask();
		variables.clear();
		variables.put("run", 1);

		long count = activitiRule.getRuntimeService().createProcessInstanceQuery().count();

		Assert.assertEquals(1, count);

		complete(currentTask, variables);

		count = activitiRule.getRuntimeService().createProcessInstanceQuery().count();

		Assert.assertEquals(0, count);

		processInstance = processEngine.getRuntimeService()
				.startProcessInstanceByKey("testProcess", "myTest", variables);

		currentTask = currentTask();
		variables.clear();
		variables.put("run", 1);

		count = activitiRule.getRuntimeService().createProcessInstanceQuery().count();

		Assert.assertEquals(1, count);

		complete(currentTask, variables);

		count = activitiRule.getRuntimeService().createProcessInstanceQuery().count();

		Assert.assertEquals(0, count);

	}

	private void complete(Task task) {
		activitiRule.getTaskService().complete(task.getId());
	}

	private void complete(Task task, Map<String, Object> variables) {
		activitiRule.getTaskService().complete(task.getId(), variables);
	}

	private Task currentTask() {
		return activitiRule.getProcessEngine()
				.getTaskService().createTaskQuery()
				.active().singleResult();
	}
}
