package absyn;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import bytecode.NEWSTRING;
import bytecode.RETURN;
import semantical.TypeChecker;
import translation.Block;
import types.ClassMemberSignature;
import types.ClassType;
import types.TestSignature;
import types.VoidType;


public class TestDeclaration extends CodeDeclaration{

	private final String name;
	private final Command body;

	public TestDeclaration(int pos, Command body, ClassMemberDeclaration next, String name) {
		super(pos, null, body, next);
		
		this.body = body;
		this.name = name;
	}

	public Command getBody() {
		return body;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public TestSignature getSignature() {
		return (TestSignature) super.getSignature();
	}

	@Override
	protected void toDotAux(FileWriter where) throws IOException {
		linkToNode("body", getBody().toDot(where), where);
		linkToNode("name", toDot(name, where), where);
		
	}

	@Override
	protected void addTo(ClassType clazz) {
		TestSignature tSig = new TestSignature(clazz, this.name, this);

		clazz.addTest(name, tSig);

			// we record the signature of this constructor inside this abstract syntax
			setSignature(tSig);
		
	}

	@Override
	protected void typeCheckAux(ClassType clazz) {
		TypeChecker checker;
		
		// we build a type-checker which signals errors for the source code
		// of the class where this test is defined,
		// whose only variables in scope is this of type
		// clazz and the name of the test, and
		// where return instructions of type returnType are allowed
		checker = new TypeChecker(VoidType.INSTANCE,clazz.getErrorMsg(), true);

		// we type-check the body of the test in the resulting type-checker
		getBody().typeCheck(checker);

		// we check that there is no dead-code in the body of the test
		boolean stopping = getBody().checkForDeadcode();

		// we check that if in the body there is a deadcode
		if (stopping)
			error(checker, "Deadcode in test's body");
		
	}
	
	public void translate(Set<ClassMemberSignature> done) {
    	if (done.add(getSignature())) {
    		// we translate the body of the constructor or
    		// method with a block containing RETURN as continuation. This way,
    		// all methods returning void and
    		// with some missing return command are correctly
    		// terminated anyway. If the method is not void, this
    		// precaution is useless since we know that every execution path
    		// ends with a return command, as guaranteed by
    		// checkForDeadCode() (see typeCheck() in MethodDeclaration.java)
    		Block ret = new Block(new RETURN(ClassType.mk("String")));
    		ret = new NEWSTRING( "passed" ).followedBy(ret);
    		
    		getSignature().setCode(getBody().translate(ret));

    		// we translate all methods and constructors that are referenced
    		// from the code we have generated
    		translateReferenced(getSignature().getCode(), done, new HashSet<Block>());
    	}
    }
	
}
