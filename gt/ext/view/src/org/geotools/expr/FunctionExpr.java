 /*
  *    Geotools2 - OpenSource mapping toolkit
  *    http://geotools.org
  *    (C) 2002, Geotools Project Managment Committee (PMC)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  *
  */
package org.geotools.expr;

import java.io.IOException;

import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.FunctionExpression;


public class FunctionExpr extends AbstractExpr {
	Expr expr[];	
	String name;	
	public FunctionExpr( String name, Expr expr ){		
		this.name = name;
		this.expr = new Expr[]{ expr, };
	}
	public FunctionExpr( String name, Expr expr1, Expr expr2 ){		
		this.name = name;
		this.expr = new Expr[]{ expr1, expr2 };
	}
	public FunctionExpr( String name, Expr expr[] ){		
		this.name = name;
		this.expr = expr;
	}
	public Expression expression(FeatureType schema) throws IOException {
		FunctionExpression fn = factory.createFunctionExpression( name );
		Expression args[] = new Expression[ expr.length ];
		for( int i=0; i<expr.length; i++ ){
			args[i] = expr[i].expression( schema );
		}			
		fn.setArgs( args );
		return fn;			
	}
}