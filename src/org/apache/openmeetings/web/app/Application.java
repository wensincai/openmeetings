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
package org.apache.openmeetings.web.app;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.openmeetings.web.pages.MainPage;
import org.apache.openmeetings.web.pages.auth.SignInPage;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.mapper.HomePageMapper;
import org.apache.wicket.markup.MarkupFactory;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.session.ISessionStore.UnboundListener;
import org.apache.wicket.settings.IPageSettings;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class Application extends AuthenticatedWebApplication {
	private ConcurrentHashMap<String, WebSession> liveSessions = new ConcurrentHashMap<String, WebSession>(100); //TODO need to investigate
	
	@Override
	protected void init() {
		IPageSettings pageSettings = getPageSettings();
		pageSettings.addComponentResolver(new MessageResolver());
		pageSettings.addComponentResolver(new MessageTagHandler());
		getMarkupSettings().setMarkupFactory(new MarkupFactory(){
			@Override
			public MarkupParser newMarkupParser(MarkupResourceStream resource) {
				MarkupParser mp = super.newMarkupParser(resource);
				mp.add(new MessageTagHandler());
				return mp;
			}
		});
		super.init();
		mountPage("signin", getSignInPageClass());

		getRootRequestMapperAsCompound().add(new HomePageMapper(getHomePage()) {
			@Override
			protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
				//Does nothing
			}
			
			@Override
			public Url mapHandler(IRequestHandler requestHandler) {
				if (requestHandler instanceof ListenerInterfaceRequestHandler) {
					return null;
				} else {
					return super.mapHandler(requestHandler);
				}
			}
		});
		
		getSessionStore().registerUnboundListener(new UnboundListener() {
			public void sessionUnbound(String sessionId) {
				liveSessions.remove(sessionId);
			}
		});
	}
	
	void addLiveSession(WebSession session) {
		if (!liveSessions.containsKey(session.getId())) {
			liveSessions.put(session.getId(), session);
		}
	}
	
	@Override
	public Class<? extends Page> getHomePage() {
		return MainPage.class;
	}

	@Override
	protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
		return WebSession.class;
	}

	@Override
	public Class<? extends WebPage> getSignInPageClass() {
		return SignInPage.class;
	}
	
	public static Application get() {
		return (Application) WebApplication.get();
	}
	
	public static <T> T getBean(Class<T> clazz) {
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(get().getServletContext());
		return context.getBean(clazz);
	}
}
