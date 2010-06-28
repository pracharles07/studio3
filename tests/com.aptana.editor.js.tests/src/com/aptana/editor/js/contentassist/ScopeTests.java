package com.aptana.editor.js.contentassist;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Path;

import com.aptana.editor.js.parsing.JSParser;
import com.aptana.editor.js.parsing.ast.JSNode;
import com.aptana.editor.js.tests.FileContentBasedTests;
import com.aptana.parsing.ParseState;
import com.aptana.parsing.Scope;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.IRange;

public class ScopeTests extends FileContentBasedTests
{
	private static final String CURSOR = "${cursor}";
	private static final int CURSOR_LENGTH = CURSOR.length();
	
	/**
	 * getAST
	 * 
	 * @param source
	 * @return
	 * @throws Exception
	 */
	protected IParseNode getAST(String source) throws Exception
	{
		JSParser parser = new JSParser();
		ParseState parseState = new ParseState();

		parseState.setEditState(source, source, 0, 0);
		parser.parse(parseState);

		return parseState.getParseResult();
	}
	
	/**
	 * getSymbols
	 * 
	 * @param resource
	 * @return
	 * @throws Exception 
	 */
	protected Scope<JSNode> getSymbols(String resource) throws Exception
	{
		// get source from resource
		File file = this.getFile(new Path(resource));
		String source = this.getContent(file);
		
		// find all test points and clean up source along the way
		List<Integer> offsets = new LinkedList<Integer>();
		int offset = source.indexOf(CURSOR);

		while (offset != -1)
		{
			offsets.add(offset);
			source = source.substring(0, offset) + source.substring(offset + CURSOR_LENGTH);
			offset = source.indexOf(CURSOR);
		}

		// parser
		JSParser parser = new JSParser();
		ParseState parseState = new ParseState();

		parseState.setEditState(source, source, 0, 0);
		parser.parse(parseState);
		
		return parser.getScope();
	}
	
	/**
	 * getTypes
	 * 
	 * @param symbols
	 * @param symbol
	 * @return
	 */
	protected List<String> getTypes(Scope<JSNode> symbols, String symbol)
	{
		List<JSNode> nodes = symbols.getLocalSymbol(symbol);
		Set<String> typeSet = new HashSet<String>();
		
		for (JSNode node : nodes)
		{
			JSTypeWalker typeWalker = new JSTypeWalker(symbols);
			
			typeWalker.visit(node);
			
			List<String> types = typeWalker.getTypes();
			
			if (types != null)
			{
				typeSet.addAll(types);
			}
		}
		
		return new LinkedList<String>(typeSet);
	}
	
	/**
	 * showSymbols
	 * 
	 * @param symbols
	 */
	protected void showSymbols(String title, Scope<JSNode> symbols)
	{
		IRange range = symbols.getRange();
		
		System.out.println(title);
		System.out.println("====");
		System.out.println("Globals(" + range.getStartingOffset() + "," + range.getEndingOffset() + ")");
		this.showSymbols(symbols, "");
		System.out.println();
	}
	
	/**
	 * showSymbols
	 * 
	 * @param symbols
	 * @param indent
	 */
	protected void showSymbols(Scope<JSNode> symbols, String indent)
	{
		for (String symbol : symbols.getLocalSymbolNames())
		{
			List<String> types = this.getTypes(symbols, symbol);
			
			System.out.print(indent);
			System.out.println(symbol + ": " + types);
		}
		
		for (Scope<JSNode> child : symbols.getChildren())
		{
			IRange range = child.getRange();
			
			System.out.print(indent);
			System.out.println("Child(" + range.getStartingOffset() + "," + range.getEndingOffset() + ")");
			
			this.showSymbols(child, indent + "  ");
		}
	}
	
	/**
	 * testGlobalNamedFunction
	 * 
	 * @throws Exception
	 */
	public void testGlobalNamedFunction() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/globalNamedFunction.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(1, names.size());
		assertEquals("globalFunction", names.get(0));
		
		// globalFunction
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		Scope<JSNode> child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(0, names.size());
		
		//this.showSymbols("globalNamedFunction.js", symbols);
	}
	
	/**
	 * testGlobalVarFunction
	 * 
	 * @throws Exception
	 */
	public void testGlobalVarFunction() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/globalVarFunction.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(1, names.size());
		assertEquals("globalVarFunction", names.get(0));
		
		// globalVarFunction
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		Scope<JSNode> child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(0, names.size());
		
		//this.showSymbols("globalVarFunction.js", symbols);
	}
	
	/**
	 * testGlobalNamedVarFunction
	 * 
	 * @throws Exception
	 */
	public void testGlobalNamedVarFunction() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/globalNamedVarFunction.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(2, names.size());
		assertTrue(names.contains("globalVarFunction"));
		assertTrue(names.contains("globalFunction"));
		
		// globalVarFunction/globalFunction
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		Scope<JSNode> child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(0, names.size());
		
		//this.showSymbols("globalNamedVarFunction.js", symbols);
	}
	
	/**
	 * testGlobalVars
	 * 
	 * @throws Exception
	 */
	public void testGlobalVars() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/globalVars.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(6, names.size());
		assertTrue(names.contains("localVar1"));
		assertTrue(names.contains("localVar2"));
		assertTrue(names.contains("localVar3"));
		assertTrue(names.contains("localVar4"));
		assertTrue(names.contains("localVar5"));
		assertTrue(names.contains("localVar6"));
		
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(0, children.size());
		
		//this.showSymbols("globalVars.js", symbols);
	}
	
	/**
	 * testLocalVars
	 * 
	 * @throws Exception
	 */
	public void testLocalVars() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/localVars.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names =  symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(1, names.size());
		assertEquals("globalFunction", names.get(0));
		
		// globalFunction
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		Scope<JSNode> child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(6, names.size());
		assertTrue(names.contains("localVar1"));
		assertTrue(names.contains("localVar2"));
		assertTrue(names.contains("localVar3"));
		assertTrue(names.contains("localVar4"));
		assertTrue(names.contains("localVar5"));
		assertTrue(names.contains("localVar6"));
		
		//this.showSymbols("localVars.js", symbols);
	}
	
	/**
	 * testParameters
	 * 
	 * @throws Exception
	 */
	public void testParameters() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/parameters.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(1, names.size());
		assertEquals("globalFunction", names.get(0));
		
		// globalFunction
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		Scope<JSNode> child = children.get(0);
		names = child.getLocalSymbolNames();
		assertEquals(2, names.size());
		assertTrue(names.contains("parameter1"));
		assertTrue(names.contains("parameter2"));
		
		//this.showSymbols("parameters.js", symbols);
	}
	
	/**
	 * testNestedFunctions
	 * 
	 * @throws Exception
	 */
	public void testNestedFunctions() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/nestedFunctions.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(1, names.size());
		assertEquals("outerFunction", names.get(0));
		
		// outerFunction
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		Scope<JSNode> child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(3, names.size());
		assertTrue(names.contains("innerFunction"));
		assertTrue(names.contains("outerParam1"));
		assertTrue(names.contains("outerParam2"));
		
		// innerFunction
		children = child.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(2, names.size());
		assertTrue(names.contains("innerParam1"));
		assertTrue(names.contains("innerParam2"));
		
		//this.showSymbols("nestedFunctions.js", symbols);
	}
	
	/**
	 * testNestedFunctions2
	 * 
	 * @throws Exception
	 */
	public void testNestedFunctions2() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/nestedFunctions2.js");
		List<String> names;
		
		// global
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(3, names.size());
		assertTrue(names.contains("global1"));
		assertTrue(names.contains("global2"));
		assertTrue(names.contains("functionA"));
		
		// functionA
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(1, children.size());
		
		Scope<JSNode> child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(5, names.size());
		assertTrue(names.contains("functionAParam1"));
		assertTrue(names.contains("functionAParam2"));
		assertTrue(names.contains("functionALocal"));
		assertTrue(names.contains("functionB"));
		assertTrue(names.contains("functionB2"));
		
		children = child.getChildren();
		assertNotNull(children);
		assertEquals(2, children.size());
		
		// functionB
		child = children.get(0);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(4, names.size());
		assertTrue(names.contains("functionBParam1"));
		assertTrue(names.contains("functionBParam2"));
		assertTrue(names.contains("functionBLocal"));
		assertTrue(names.contains("functionC"));
		
		// functionC
		List<Scope<JSNode>> grandchildren = child.getChildren();
		assertNotNull(grandchildren);
		assertEquals(1, grandchildren.size());
		
		Scope<JSNode> grandchild = grandchildren.get(0);
		names = grandchild.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(3, names.size());
		assertTrue(names.contains("functionCParam1"));
		assertTrue(names.contains("functionCParam2"));
		assertTrue(names.contains("functionCLocal"));
		
		// functoinB2
		child = children.get(1);
		names = child.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(2, names.size());
		assertTrue(names.contains("functionB2Param"));
		assertTrue(names.contains("functionB2Local"));
		
		//this.showSymbols("nestedFunctions2.js", symbols);
	}
	
	/**
	 * testPrimitives
	 * 
	 * @throws Exception
	 */
	public void testPrimitives() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/primitives.js");
		List<String> names;
		
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(8, names.size());
		assertTrue(names.contains("booleanTrue"));
		assertTrue(names.contains("booleanFalse"));
		assertTrue(names.contains("doubleQuotedString"));
		assertTrue(names.contains("singleQuotedString"));
		assertTrue(names.contains("array"));
		assertTrue(names.contains("object"));
		assertTrue(names.contains("number"));
		assertTrue(names.contains("regex"));
		
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(0, children.size());
		
		//this.showSymbols("primitives.js", symbols);
	}
	
	/**
	 * testMultipleTypes
	 * 
	 * @throws Exception
	 */
	public void testMultipleTypes() throws Exception
	{
		Scope<JSNode> symbols = this.getSymbols("ast-queries/multipleTypes.js");
		List<String> names;
		
		assertNotNull(symbols);
		names = symbols.getLocalSymbolNames();
		assertNotNull(names);
		assertEquals(1, names.size());
		assertEquals("stringAndNumber", names.get(0));
		
		List<Scope<JSNode>> children = symbols.getChildren();
		assertNotNull(children);
		assertEquals(0, children.size());
		
		//this.showSymbols("multipleTypes.js", symbols);
	}
}
