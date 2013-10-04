/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.apache.openmeetings.data.user.UserManager;
import org.apache.openmeetings.db.dao.basic.ConfigurationDao;
import org.apache.openmeetings.db.dao.calendar.AppointmentDao;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.entity.calendar.Appointment;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.installation.ImportInitvalues;
import org.apache.openmeetings.installation.InstallationConfig;
import org.apache.openmeetings.util.OmFileHelper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({})
@ContextConfiguration(locations={"classpath:openmeetings-applicationContext.xml"}, inheritLocations = true)
public abstract class AbstractOpenmeetingsSpringTest extends AbstractJUnit4SpringContextTests {
	private static final Logger log = Red5LoggerFactory.getLogger(AbstractOpenmeetingsSpringTest.class);
	
	protected static final String username = "swagner";
	protected static final String userpass = "qweqwe";
	private static final String orgname = "smoketest";
	private static final String timeZone = "Europe/Berlin";
	private static final String useremail = "junit@openmeetings.apache.org";
	
	@Autowired
	private AppointmentDao appointmentDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserDao userDao;
	@Autowired
	private ImportInitvalues importInitvalues;
	@Autowired
	private ConfigurationDao configurationDao;

	@Before
	public void setUp() throws Exception {
		setOmHome();
        if (userDao.count() < 1) {
            makeDefaultScheme();
            log.info("Default scheme created successfully");
        } else {
            log.info("Default scheme already created");
        }
    }

	public Appointment createAppointment() throws Exception {
		assertNotNull("Can't access to appointment dao implimentation", appointmentDao);

		// add new appointment
		Appointment ap = new Appointment();

		ap.setTitle("appointmentName");
		ap.setLocation("appointmentLocation");

		Date appointmentstart = new Date();
		Date appointmentend = new Date();
		appointmentend.setTime(appointmentstart.getTime() + 3600);

		ap.setStart(appointmentstart);
		ap.setEnd(appointmentend);
		ap.setDescription("appointmentDescription");
		ap.setInserted(new Date());
		ap.setDeleted(false);
		ap.setIsDaily(false);
		ap.setIsWeekly(false);
		ap.setIsMonthly(false);
		ap.setIsYearly(false);
		ap.setPasswordProtected(false);

		ap.setOwner(userDao.get(1L));
		ap.setConnectedEvent(false);
		Long id = appointmentDao.addAppointmentObj(ap);
		assertNotNull("Cann't add appointment", id);
		return ap;
	}

	public User createUser(int rnd) throws Exception {
		User u = new User();
		// add user
		u.setFirstname("firstname" + rnd);
		u.setLastname("lastname" + rnd);
		u.setLogin("login");
		u.updatePassword(configurationDao, "pass" + rnd);
		u.setLanguage_id(1L);
		Long user_id = userManager.addUser(u);
		assertTrue("Cann't add user", user_id > 0);
		u = userManager.getUserByIdAndDeleted(user_id);
		assertNotNull("User should not be null", u);
		return u;
	}

	protected void setOmHome() {
		String webappsDir = System.getProperty("om.home", ".");
		OmFileHelper.setOmHome(webappsDir);
		if (!OmFileHelper.getOmHome().exists() || !OmFileHelper.getOmHome().isDirectory()) {
			fail("Invalid directory is specified as OM HOME: " + webappsDir);
		}
	}
	
	private void makeDefaultScheme() throws Exception {
		InstallationConfig cfg = new InstallationConfig();
		cfg.username = username;
		cfg.password = userpass;
		cfg.email = useremail;
		cfg.group = orgname;
		cfg.ical_timeZone = timeZone;
		importInitvalues.loadAll(cfg, false);
	}

	public User createUserContact(int rnd, Long ownerId) throws Exception {
		User user = userDao.getContact("email" + rnd, "firstname" + rnd, "lastname" + rnd, ownerId);
		user = userDao.update(user, ownerId);
		assertNotNull("Cann't add user", user);
		return user;
	}

}
