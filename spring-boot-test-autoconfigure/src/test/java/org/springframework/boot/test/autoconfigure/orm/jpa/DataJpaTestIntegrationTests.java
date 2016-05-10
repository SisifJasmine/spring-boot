/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.autoconfigure.orm.jpa;

import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DataJpaTest}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class DataJpaTestIntegrationTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Autowired
	private TestEntityManager entities;

	@Autowired
	private ExampleRepository repository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void testEntityManager() throws Exception {
		ExampleEntity entity = this.entities.persist(new ExampleEntity("spring", "123"));
		this.entities.flush();
		Object id = this.entities.getId(entity);
		ExampleEntity found = this.entities.find(ExampleEntity.class, id);
		assertThat(found.getName()).isEqualTo("spring");
	}

	@Test
	public void testEntityManagerPersistAndGetId() throws Exception {
		Long id = this.entities.persistAndGetId(new ExampleEntity("spring", "123"),
				Long.class);
		assertThat(id).isNotNull();
	}

	@Test
	public void testRepository() throws Exception {
		this.entities.persist(new ExampleEntity("spring", "123"));
		this.entities.persist(new ExampleEntity("boot", "124"));
		this.entities.flush();
		ExampleEntity found = this.repository.findByReference("124");
		assertThat(found.getName()).isEqualTo("boot");
	}

	@Test
	public void replacesDefinedDataSourceWithEmbeddedDefault() throws Exception {
		String product = this.dataSource.getConnection().getMetaData()
				.getDatabaseProductName();
		assertThat(product).isEqualTo("H2");
	}

	@Test
	public void didNotInjectExampleComponent() throws Exception {
		this.thrown.expect(NoSuchBeanDefinitionException.class);
		this.applicationContext.getBean(ExampleComponent.class);
	}

	@Test
	public void flywayAutoConfigurationWasImported() {
		ConditionEvaluationReport report = ConditionEvaluationReport
				.get((ConfigurableListableBeanFactory) this.applicationContext
						.getAutowireCapableBeanFactory());
		assertThat(report.getConditionAndOutcomesBySource().keySet())
				.contains(FlywayAutoConfiguration.class.getName());
	}

	@Test
	public void liquibaseAutoConfigurationWasImported() {
		ConditionEvaluationReport report = ConditionEvaluationReport
				.get((ConfigurableListableBeanFactory) this.applicationContext
						.getAutowireCapableBeanFactory());
		assertThat(report.getConditionAndOutcomesBySource().keySet())
				.contains(LiquibaseAutoConfiguration.class.getName());
	}

}
