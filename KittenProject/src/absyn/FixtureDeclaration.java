package absyn;

import java.io.FileWriter;
import java.io.IOException;

import semantical.TypeChecker;
import types.ClassType;
import types.FixtureSignature;
import types.VoidType;


public class FixtureDeclaration extends CodeDeclaration{
	
	private final Command body;

	public FixtureDeclaration(int pos, Command body, ClassMemberDeclaration next) {
		super(pos, null, body, next);
		
		this.body = body;
	}

	public Command getBody() {
		return body;
	}

	@Override
	public FixtureSignature getSignature() {
		return (FixtureSignature) super.getSignature();
	}
	
	@Override
	protected void toDotAux(FileWriter where) throws IOException {
		linkToNode("body", getBody().toDot(where), where);
		
	}

	@Override
	protected void addTo(ClassType clazz) {
		FixtureSignature fSig = new FixtureSignature(clazz, this);

		clazz.addFixture(fSig);

			// we record the signature of this constructor inside this abstract syntax
			setSignature(fSig);
		
	}

	@Override
	protected void typeCheckAux(ClassType clazz) {
		TypeChecker checker;
		
		// we build a type-checker which signals errors for the source code
		// of the class where this fixture is defined,
		// whose only variables in scope is this of type
		// clazz and the name of the fixture, and
		// where return instructions of type returnType are allowed
		checker = new TypeChecker(VoidType.INSTANCE,clazz.getErrorMsg(), false);

		// we type-check the body of the test in the resulting type-checker
		getBody().typeCheck(checker);

		// we check that there is no dead-code in the body of the test
		boolean stopping = getBody().checkForDeadcode();

		// we check that if in the body there is a deadcode
		if (stopping)
			error(checker, "Deadcode in fixture's body");
		
	}
	
}
