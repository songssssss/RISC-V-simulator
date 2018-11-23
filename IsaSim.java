/**
 * RISC-V Instruction Set Simulator
 * 
 * @author Lau Kai Sing (laut9810@gmail.com)
 * 
 */
public class IsaSim {

	static int pc;
	static int reg[] = new int[32];

	// memory?
	// static int mem[]

	// The final simulator has to read a binary file containing RISC-V instructions.
	// What is the file type for the instruction?
	// Do I need to run file when running?

	// Here the first program hard coded as an array
	static int progr[] = {
			// As minimal RISC-V assembler example
			0x00200093, // addi x1 x0 2
			0x00300113, // addi x2 x0 3
			0x002081b3, // add x3 x1 x2
			0x40208233, // sub x4 x1 x2
	};

	public static String decToHex(int dec) {
		return Integer.toHexString(dec); // this turn decimal to hex, only 
		//used in default case
	}

	public static int getUnsignedInt(int x) {
		System.out.println("int: " + x);
		System.out.println("unsigned int: " + (x & 0xffffffff));
		return (x & 0xffffffff);
	}

	public static void main(String[] args) {

		System.out.println("Hello RISC-V World!");

		pc = 0;
		// reg[0] = 0; // Don't know if necessary

		for (;;) {

			int instr = progr[pc];
			int opcode = instr & 0x7f;
			int rd = (instr >> 7) & 0x01f;
			int funct3 = (instr >> 12) & 0x7;
			int rs1 = (instr >> 15) & 0x01f;
			int rs2 = (instr >> 20) & 0x01f;
			int imm = (instr >> 25);
			int I_imm = rs2 + (imm << 5); // for I-type
			int U_imm = funct3 + (rs1 << 5) + (rs2 << 8) + (imm << 13);
			int S_imm = rd + (imm << 5);
			int B_imm = rd + (imm << 5);
			// (rd >> 1)&0xf + (imm < 4)&0x3f; // not sure about B-type
			// int J_imm = ;

			System.out.println("instr: 0x" + decToHex(instr) + " ");
			System.out.println("opcode: 0x" + decToHex(opcode) + " ");
			System.out.println("rd: " + rd + " ");
			System.out.println("fucnt3: " + funct3 + " ");
			System.out.println("rs1: " + rs1 + " ");
			System.out.println("rs2: " + rs2 + " ");
			System.out.println("imm: " + imm + " ");

			switch (opcode) {
			// fence, fence.i, ebreak, csrrw, csrrs, csrrc, csrrwi, 
			//csrrsi, csrrci can be ignored
					
			case 0x0: // template
				switch (funct3) {
				}
				break;

			case 0x037: // LUI load upper immediate (20bits)
				reg[rd] = U_imm & 0xfffff000;
				break;

			case 0x027: // AUIPC Add Upper Imm (20bits) to PC
				reg[1] = pc; // do i need this?
				pc = U_imm & 0xfffff000;
				reg[rd] = pc;
				break;

			case 0x06f: // JAL
				reg[rd] = pc + 4;
				pc = B_imm - 4;
				break;

			case 0x067: // JALR
				reg[rd] = pc + 4;
				pc = reg[rs1] + B_imm;
				break;

			case 0x063: // type: bench
				switch (funct3) {
				case 0b000:// BEQ
					if (reg[rs1] == reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b001:// BNE
					if (reg[rs1] != reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b100: // BLT
					if (reg[rs1] < reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b101: // BGE
					if (reg[rs1] >= reg[rs2]) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b110: // BLTU
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				case 0b111:// BGEU
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
						reg[1] = pc + 4;
						pc = B_imm;
					}
					break;
				}

				break;
			case 0x003: // type:load
				// where is the memory?
				// LB
				// LH
				// LW
				// LNU
				// LHU
				break;

			case 0x023: // type: store
				// SB
				// SH
				// SW

				break;

			case 0x013: // type: immediate

				switch (funct3) {
				case 0b000: // ADDI
					reg[rd] = reg[rs1] + I_imm;
					break;
				case 0b010: // SLTI (set < immediate)
					if (reg[rs1] < I_imm) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
					break;
				case 0b011: // SLTIU (set < imm unsigned)
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(I_imm)) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
					break;
				case 0b100: // XORI
					reg[rd] = reg[rs1] ^ I_imm;
					break;
				case 0b110: // ORI
					reg[rd] = reg[rs1] | I_imm;
					break;
				case 0b111: // ANDI
					reg[rd] = reg[rs1] & I_imm;
					break;
				case 0b001: // SLLI
					reg[rd] = reg[rs1] << I_imm;
					break;
				case 0b101: // SRLI & SRAI 
					if (imm != 0) { // SRL
						reg[rd] = reg[rs1] >> I_imm;
					} else { // SRA
						// as far as I know, 
						//shifting in java keep the sign, 
						//need checking
						reg[rd] = reg[rs1] >> I_imm;
						reg[rd] = (reg[rd] & 0xffffffff);
					}
					break;
				}
				break;

			case 0x033:
				switch (funct3) {
				case 0b000: // ADD & SUB
					reg[rd] = reg[rs1] - reg[rs2] + 2 * (1 - (imm >> 5)) * reg[rs2];
					break;
				case 0b001: // SLL
					reg[rd] = reg[rs1] << reg[rs2];
					break;
				case 0b010: // SLT (set<)
					if (reg[rs1] < reg[rs2]) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
					break;
				case 0b011: // SLTU (set< unsign)
					if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
						reg[rd] = 1;
					} else {
						reg[rd] = 0;
					}
					break;
				case 0b100: // XOR
					reg[rd] = reg[rs1] ^ reg[rs2];
					break;
				case 0b101: // SRL & SRA 
					if (imm != 0) { // SRL
						reg[rd] = reg[rs1] >> reg[rs2];
					} else { // SRA
						// as far as I know, 
						// shifting in java keep the sign,
						// need checking
						reg[rd] = reg[rs1] >> reg[rs2];
						reg[rd] = (reg[rd] & 0xffffffff);
					}
					break;
				case 0b110: // OR
					reg[rd] = reg[rs1] | reg[rs2];
					break;
				case 0b111: // AND
					reg[rd] = reg[rs1] & reg[rs2];
					break;

				}
				break;

			case 0x073: // ecall
				pc = progr.length;
				// When the program ends with ecall you write another binary file containing the
				// content of your registers (the .res file).
				break;

			default:
				System.out.println("Opcode 0x" + decToHex(opcode) + " not yet implemented");
				break;
			}

			++pc; // We count in 4 byte words

			for (int i = 0; i < reg.length; ++i) {
				System.out.print(reg[i] + " ");
			}
			System.out.println();
			if (pc >= progr.length) {
				break;
			}
		}

		System.out.println("Program exit");

	}

}
