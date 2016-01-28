package absyn;

import java.io.FileWriter;

import bytecode.NEWSTRING;
import bytecode.RETURN;
import bytecode.VIRTUALCALL;
import semantical.TypeChecker;
import translation.Block;
import types.ClassType;
import types.TypeList;


public class Assert extends Command{

	/**
	 * The boolean condition of the command Assert.
	 */

	private final Expression condition;

	private String failureMsg;
	private String posFailureMsg; 
	
	/**
	 * Constructs the abstract syntax of an Assert command.
	 *
	 * @param pos the position in the source file where it starts
	 *            the concrete syntax represented by this abstract syntax
	 * @param condition the condition of the Assert
	 */

	public Assert(int pos, Expression condition) {
		super(pos);

		this.condition = condition;
	}
	
	@Override
	protected void toDotAux(FileWriter where) throws java.io.IOException {
		if(condition != null) linkToNode("condition", condition.toDot(where), where);
	}

	@Override
	protected TypeChecker typeCheckAux(TypeChecker checker) {
		condition.mustBeBoolean(checker);
		
		// we check if assert command is in a test
		if(!checker.isAssertIsAllowed())
			error(checker, "ATTENTION: Assert can only be use in a test ");
		
		//failure message with the row.column position
		failureMsg = "KITTEN: test fallito @" + checker.getFileName() + ":" + checker.getErrorPos(getPos()) + "\n";
		posFailureMsg = "failed at "+ checker.getErrorPosForMsg(getPos());
		
		// we return the original type-checker.
		return checker;
	}

	@Override
	public boolean checkForDeadcode() {
		return false;
	}

	@Override
	public Block translate(Block continuation) {
		continuation.doNotMerge();
		
		//We instantiate a new code block "failure" with a return type of String   
		Block failure = new Block(new RETURN(ClassType.mk("String")));
		//we call the output method in the String.kit file and then we use followBy()
		failure = new VIRTUALCALL(ClassType.mkFromFileName("String.kit"), ClassType.mkFromFileName("String.kit").methodLookup("output", TypeList.EMPTY)).followedBy(failure);
		//we create a new String "failureMsg"(with inside the information about the false condition) and then we use followBy()
		failure = new NEWSTRING(failureMsg).followedBy(failure);
		//we push on the stack the string posFailureMsg(used to generate javabytecode) 
		failure = new NEWSTRING(posFailureMsg).followedBy(failure);
		
		
		return condition.translateAsTest(continuation, failure);
	}

	public Expression getCondition() {
		return condition;
	}
}
