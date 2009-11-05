/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.apt.meta;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

import org.seasar.doma.Function;
import org.seasar.doma.internal.apt.AptException;
import org.seasar.doma.internal.apt.type.BasicType;
import org.seasar.doma.internal.apt.type.DataType;
import org.seasar.doma.internal.apt.type.DomainType;
import org.seasar.doma.internal.apt.type.EntityType;
import org.seasar.doma.internal.apt.type.ListType;
import org.seasar.doma.internal.apt.type.SimpleDataTypeVisitor;
import org.seasar.doma.internal.apt.util.ElementUtil;
import org.seasar.doma.internal.message.DomaMessageCode;

/**
 * @author taedium
 * 
 */
public class AutoFunctionQueryMetaFactory extends
        AutoModuleQueryMetaFactory<AutoFunctionQueryMeta> {

    public AutoFunctionQueryMetaFactory(ProcessingEnvironment env) {
        super(env);
    }

    @Override
    public QueryMeta createQueryMeta(ExecutableElement method, DaoMeta daoMeta) {
        assertNotNull(method, daoMeta);
        AnnotationMirror annotationMirror = ElementUtil.getAnnotationMirror(
                method, Function.class, env);
        if (annotationMirror == null) {
            return null;
        }
        AutoFunctionQueryMeta queryMeta = new AutoFunctionQueryMeta();
        queryMeta.setName(method.getSimpleName().toString());
        queryMeta.setExecutableElement(method);
        queryMeta.setAnnotationMirror(annotationMirror, env);
        queryMeta.setFunctionName(annotationMirror, env);
        queryMeta.setQueryKind(QueryKind.AUTO_FUNCTION);
        doTypeParameters(queryMeta, method, daoMeta);
        doReturnType(queryMeta, method, daoMeta);
        doParameters(queryMeta, method, daoMeta);
        doThrowTypes(queryMeta, method, daoMeta);
        return queryMeta;
    }

    @Override
    protected void doReturnType(AutoFunctionQueryMeta queryMeta,
            ExecutableElement method, DaoMeta daoMeta) {
        QueryReturnMeta returnMeta = createReturnMeta(method);
        queryMeta.setReturnMeta(returnMeta);
        ResultParameterMeta resultParameterMeta = createCallableSqlResultParameterMeta(returnMeta);
        queryMeta.setResultParameterMeta(resultParameterMeta);
    }

    protected ResultParameterMeta createCallableSqlResultParameterMeta(
            final QueryReturnMeta returnMeta) {
        return returnMeta
                .getDataType()
                .accept(
                        new SimpleDataTypeVisitor<ResultParameterMeta, Void, RuntimeException>() {

                            @Override
                            protected ResultParameterMeta defaultAction(
                                    DataType type, Void p)
                                    throws RuntimeException {
                                throw new AptException(
                                        DomaMessageCode.DOMA4063, env,
                                        returnMeta.getElement(), returnMeta
                                                .getType());
                            }

                            @Override
                            public ResultParameterMeta visitBasicType(
                                    BasicType dataType, Void p)
                                    throws RuntimeException {
                                return new BasicResultParameterMeta(dataType);
                            }

                            @Override
                            public ResultParameterMeta visitDomainType(
                                    DomainType dataType, Void p)
                                    throws RuntimeException {
                                return new DomainResultParameterMeta(dataType);
                            }

                            @Override
                            public ResultParameterMeta visitListType(
                                    ListType dataType, Void p)
                                    throws RuntimeException {
                                return dataType
                                        .getElementType()
                                        .accept(
                                                new SimpleDataTypeVisitor<ResultParameterMeta, Void, RuntimeException>() {

                                                    @Override
                                                    protected ResultParameterMeta defaultAction(
                                                            DataType dataType,
                                                            Void p)
                                                            throws RuntimeException {
                                                        throw new AptException(
                                                                DomaMessageCode.DOMA4065,
                                                                env,
                                                                returnMeta
                                                                        .getElement(),
                                                                dataType
                                                                        .getTypeName());
                                                    }

                                                    @Override
                                                    public ResultParameterMeta visitBasicType(
                                                            BasicType dataType,
                                                            Void p)
                                                            throws RuntimeException {
                                                        return new BasicListResultParameterMeta(
                                                                dataType);
                                                    }

                                                    @Override
                                                    public ResultParameterMeta visitDomainType(
                                                            DomainType dataType,
                                                            Void p)
                                                            throws RuntimeException {
                                                        return new DomainListResultParameterMeta(
                                                                dataType);
                                                    }

                                                    @Override
                                                    public ResultParameterMeta visitEntityType(
                                                            EntityType dataType,
                                                            Void p)
                                                            throws RuntimeException {
                                                        return new EntityListResultParameterMeta(
                                                                dataType);
                                                    }

                                                }, p);
                            }

                        }, null);
    }
}
