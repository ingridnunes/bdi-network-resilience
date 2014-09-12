//----------------------------------------------------------------------------
// Copyright (C) 2011  Ingrid Nunes
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://inf.ufrgs.br/prosoft/bdi4jade/
//
//----------------------------------------------------------------------------
package br.ufrgs.inf.bdinetr.domain.ontology;

import jade.content.abs.AbsObject;
import jade.content.abs.AbsPrimitive;
import jade.content.onto.BCReflectiveIntrospector;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ObjectSchema;

import java.lang.reflect.Method;

/**
 * @author Ingrid Nunes
 */
public class Introspector extends BCReflectiveIntrospector {

	/**
	 * <p>
	 * UID generated.
	 * </p>
	 */
	private static final long serialVersionUID = 2464170261735572000L;

	@Override
	public AbsObject externalizeSpecialType(Object obj, ObjectSchema schema,
			Class javaClass, Ontology referenceOnto) throws OntologyException {
		if (schema instanceof EnumerationSchema) {
			return AbsPrimitive.wrap(obj.toString());
		} else {
			return super.externalizeSpecialType(obj, schema, javaClass,
					referenceOnto);
		}
	}

	@Override
	protected void invokeSetterMethod(Method method, Object obj, Object value)
			throws OntologyException {
		if (method.getParameterTypes()[0].isEnum()) {
			Object[] enumContants = method.getParameterTypes()[0]
					.getEnumConstants();
			for (Object enumValue : enumContants) {
				if (value.equals(enumValue.toString())) {
					Object[] params = new Object[] { enumValue };
					try {
						method.invoke(obj, params);
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}

		super.invokeSetterMethod(method, obj, value);
	}

}
