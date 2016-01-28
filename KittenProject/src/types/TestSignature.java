package types;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.MethodGen;

import javaBytecodeGenerator.JavaClassGenerator;
import absyn.TestDeclaration;
import translation.Block;

/**
 * The signature of a method of a Kitten class.
 */

public class TestSignature extends CodeSignature {
	
	private static Type KITTEN_STRING = ClassType.mk( runTime.String.class.getSimpleName() );

	public TestSignature(ClassType clazz, String name, TestDeclaration abstractSyntax) {

		super(clazz,VoidType.INSTANCE,TypeList.EMPTY,name,abstractSyntax);
	}
	
	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}

	public void createTest(JavaClassGenerator classGen) {
		
		MethodGen methodGen = new MethodGen(Constants.ACC_PRIVATE | Constants.ACC_STATIC, // private and static
				KITTEN_STRING.toBCEL(), // return type
				new org.apache.bcel.generic.Type[] // parameters
					{ this.getDefiningClass().toBCEL() },
				null, // parameters names: we do not care
				getName(), // method's name
				classGen.getClassName(), // defining class
				// TODO devo aggiungere a getCode() il codice: .linkTo(new Block(new RETURN( KITTEN_STRING )))??
				classGen.generateJavaBytecode(getCode()), // bytecode of the method   
				classGen.getConstantPool()); // constant pool
		
		// we must always call these methods before the getMethod()
		// method below. They set the number of local variables and stack
		// elements used by the code of the method
		methodGen.setMaxStack();
		methodGen.setMaxLocals();

		// we add a method to the class that we are generating
		classGen.addMethod(methodGen.getMethod());
		
	}
	
	public INVOKESTATIC createINVOKESTATIC(JavaClassGenerator classGen) {
		 // we use the instruction factory in order to put automatically inside
		// the constant pool a reference to the Java signature of this method or constructor
		
		return (INVOKESTATIC) classGen.getFactory().createInvoke
  			(getDefiningClass().toBCEL().toString()+"Test", // name of the class
			getName().toString(), // name of the method or constructor
			KITTEN_STRING.toBCEL(), // return type
			new org.apache.bcel.generic.Type[] { getDefiningClass().toBCEL() }, // parameters types
			Constants.INVOKESTATIC); // the type of invocation (static, special, ecc.)
	}

}
