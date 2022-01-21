/*
 *  Copyright 2022 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.codebreaker.configuration;

import java.security.SecureRandom;
import java.util.Random;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Utility class providing {@link Bean}-annotated methods to satisfy dependencies (declared in
 * Spring components) on instances of classes not declared as Spring beans, as well as {@code
 * static} methods for use by non-Spring components to access Spring beans.
 */
@Configuration
public class Beans implements ApplicationContextAware {

  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext context) {
    Beans.context = context;
  }

  /**
   * Returns a Spring bean instance of the specified type, if such an instance exists.
   *
   * @param beanType Required type (class or interface) of requested bean.
   * @param <T>      Bean type parameter.
   * @return Bean instance of requested {@code beanType}.
   * @throws NoSuchBeanDefinitionException   If no Spring bean of the requested type (or a subtype
   *                                         of the requested type) has been defined.
   * @throws NoUniqueBeanDefinitionException If more than one Spring bean of the requested type is
   *                                         available.
   * @throws BeansException                  If instantiation of the requested bean fails.
   */
  public static <T> T bean(Class<T> beanType)
      throws NoSuchBeanDefinitionException, NoUniqueBeanDefinitionException, BeansException {
    return context.getBean(beanType);
  }

  /**
   * Returns a Spring bean instance with the specified declared name, if such an instance exists.
   * Note that the instance returned (if any) will require a cast to the relevant type.
   *
   * @param name Requested bean name.
   * @return {@link Object} instance of bean with specified name.
   * @throws NoSuchBeanDefinitionException If no Spring bean has been defined with the specified
   *                                       name.
   * @throws BeansException                If instantiation of the requested bean fails.
   */
  public static Object bean(String name) throws NoSuchBeanDefinitionException, BeansException {
    return context.getBean(name);
  }

  /**
   * Returns an instance of {@link Random} (or a subclass), which may be used by Spring to satisfy
   * an explicit bean request or a dependency declared by a Spring component.
   *
   * @return
   */
  @Bean
  public Random getRandom() {
    return new SecureRandom();
  }

}
