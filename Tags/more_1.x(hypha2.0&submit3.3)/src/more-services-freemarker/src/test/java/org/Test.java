/*
 * Copyright 2008-2009 the original author or authors.
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
package org;
import java.io.OutputStreamWriter;
import org.more.hypha.context.app.ClassPathApplicationContext;
import org.more.services.freemarker.FreemarkerService;
import org.more.services.freemarker.assembler.FreemarkerService_Impl;
public class Test {
    @org.junit.Test
    public void testFK() throws Throwable {
      //  ClassPathApplicationContext context = new ClassPathApplicationContext();
        //context.init();
        //FreemarkerService service = context.getService(FreemarkerService.class);
        FreemarkerService service = new FreemarkerService_Impl();
        service.start();
        System.out.println(service);
        System.out.println();
        System.out.println();
        System.out.println();
        
        service.addTemplate("500", "root/error-500.html");
        
        service.process("500", new OutputStreamWriter(System.out));
    }
}