package types;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.MethodGen;

import javaBytecodeGenerator.JavaClassGenerator;
import absyn.FixtureDeclaration;
import translation.Block;

/**
 * The signature of a method of a Kitten class.
 */

public class FixtureSignature extends CodeSignature {

	private static Type STRING_TYPE = ClassType.mk(runTime.String.class.getSimpleName());
	
	private static int i = 1;
	
	public FixtureSignature(ClassType clazz, FixtureDeclaration abstractSyntax) {

			super(clazz,VoidType.INSTANCE,TypeList.EMPTY,"fixture" + i++,abstractSyntax);
		}
	
	@Override
	protected Block addPrefixToCode(Block code) {
		return code;
	}

    @Override
    public int hashCode() {
    	return toString().hashCode();
    }
    
	@Override
    public String toString() {
    	return getDefiningClass() + "." + getName();
    }

	public void createFixture(JavaClassGenerator classGen) {
		
		MethodGen methodGen = new MethodGen(Constants.ACC_PRIVATE | Constants.ACC_STATIC, // private and static
				org.apache.bcel.generic.Type.VOID, // return type
				new org.apache.bcel.generic.Type[] // parameters
					{ this.getDefiningClass().toBCEL() },
				null, // parameters names: we do not care
				getName(), // method's name (with the fixture's number)
				classGen.getClassName(), // defining class
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
			STRING_TYPE.toBCEL(), // return type
			new org.apache.bcel.generic.Type[] { getDefiningClass().toBCEL() }, // parameters types
			Constants.INVOKESTATIC); // the type of invocation (static, special, ecc.)
	}
}
