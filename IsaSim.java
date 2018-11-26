
/**
 * RISC-V Instruction Set Simulator
 * 
 * @author Lau Kai Sing (laut9810@gmail.com)
 * @author Song Zi Wei s181620
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class IsaSim {
    public static final String fileName = "loop";
    public static final String INPUT_FILE_NAME = fileName + ".bin";
    public static final String OUTPUT_FILE_NAME = fileName + "_copy.res";

    static int pc;
    static int reg[] = new int[32];
    static byte data[] = new byte[3000000];// memory = data[] + prog[]
    static Stack stack = new Stack();

    // Here the first program hard coded as an array
    static int instructions[] = {};
    // // As minimal RISC-V assembler example
    // 0x00200093, // addi x1 x0 2
    // 0x00300113, // addi x2 x0 3
    // 0x002081b3, // add x3 x1 x2
    // 0x40208233, // sub x4 x1 x2
    // };

    public static void log(Object thing) {
        System.out.println(String.valueOf(thing));
    }

    public static String decToHex(int dec) {
        return Integer.toHexString(dec); // this turn decimal to hex, only
        // used in default case
    }

    public static int getUnsignedInt(int x) {
        System.out.println("int: " + x);
        System.out.println("unsigned int: " + (x & 0xffffffff));
        return (x & 0xffffffff);
    }

    // public static byte[] intToByteArray(int a) {
    // return new byte[] { (byte) ((a >> 24) & 0xFF), (byte) ((a >> 16) & 0xFF),
    // (byte) ((a >> 8) & 0xFF),
    // (byte) (a & 0xFF) };
    // }

    public static byte[] intTo4Byte(int i) {
        // this disect int into byte[4]
        byte[] result = new byte[4];
        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /* >> 0 */);

        return result;
    }

    public static int byteToInt(byte[] b) {
        // this merge byte[4] to int
        int MASK = 0xFF;
        int result = 0;
        result = b[3] & MASK;
        result = result + ((b[2] & MASK) << 8);
        result = result + ((b[1] & MASK) << 16);
        result = result + ((b[0] & MASK) << 24);
        return result;
    }

    public static int[] convert(byte buf[]) {// this convert byte [] to int []
        int intArr[] = new int[buf.length / 4];
        int offset = 0;
        for (int i = 0; i < intArr.length; i++) {
            // intArr[i] = (buf[3 + offset] & 0xFF) | ((buf[2 + offset] & 0xFF) << 8) |
            // ((buf[1 + offset] & 0xFF) << 16)
            // | ((buf[0 + offset] & 0xFF) << 24);
            // offset += 4;
            intArr[i] = ((buf[3 + offset] & 0xFF) << 24) | ((buf[2 + offset] & 0xFF) << 16)
                    | ((buf[1 + offset] & 0xFF) << 8) | ((buf[0 + offset] & 0xFF));
            offset += 4;
        }
        return intArr;
    }

    public static byte[] regArrayToByteArray(int reg[]) {
        // this convert the register array to byte array for output
        int offset = 0;
        byte byteArr[] = new byte[reg.length * 4];
        byte temp[] = new byte[4];
        for (int i = 0; i < reg.length; i++) {
            temp = intTo4Byte(reg[i]);
            byteArr[offset + 0] = temp[3];
            byteArr[offset + 1] = temp[2];
            byteArr[offset + 2] = temp[1];
            byteArr[offset + 3] = temp[0];
            offset += 4;
        }
        return byteArr;

    }

    public static class SmallBinaryFiles {

        final static String FILE_NAME = " ";
        final static String OUTPUT_FILE_NAME = " ";

        byte[] readSmallBinaryFile(String fileName) throws IOException {
            Path path = Paths.get(fileName);
            return Files.readAllBytes(path);
        }

        void writeSmallBinaryFile(byte[] bytes, String fileName) throws IOException {
            Path path = Paths.get(fileName);
            Files.write(path, bytes); // creates, overwrites
        }

        void log(Object msg) {
            System.out.println(String.valueOf(msg));
        }
    }

    public static void main(String[] args) throws IOException {

        System.out.println("Hello RISC-V World!");

        pc = 0;
        // reg[0] = 0; 
        SmallBinaryFiles binary = new SmallBinaryFiles();
        byte[] bytes = binary.readSmallBinaryFile(INPUT_FILE_NAME);
        log("Name of file: " + INPUT_FILE_NAME);
        log("Small - size of file read in:" + bytes.length);
        instructions = convert(bytes);

        for (;;) {
            int instr = instructions[pc];
            int opcode = instr & 0x7f;
            int rd = (instr >> 7) & 0x01f;
            int funct3 = (instr >> 12) & 0x7;
            int rs1 = (instr >> 15) & 0x01f;
            int rs2 = (instr >> 20) & 0x01f;
            int imm = (instr >>> 25);
            int I_imm = (rs2 | (imm << 5)); // for I-type
            I_imm = ((I_imm << 20) >> 20);
            int U_imm = (funct3 | (rs1 << 5) | (rs2 << 8) | (imm << 13));
            int S_imm = rd | ((imm & 0x7f) << 5);
            S_imm = ((S_imm << 20) >> 20);
            int B_imm = (rd >> 1) & 0xf | ((imm & 0x3f) << 4) | ((rd & 0x1) << 10) | ((imm >> 6) << 11);
            B_imm = ((B_imm << 20) >> 20) / 2;
            int J_imm = (rs2 >>> 1) | (imm & 0x3f) << 4 | ((rs2 & 0x1) << 10) | (funct3 & 0x7) << 11 | (rs1 << 14)
                    | (imm >>> 6) << 19;// 7,5,5,3 => 20,10:1,11,19:12 =>7, 6:1,5:2,1,5,3
            J_imm = J_imm / 2;

            System.out.println("instr: 0x" + decToHex(instr) + " ");
            System.out.println("opcode: 0x" + decToHex(opcode) + " ");
            System.out.println("rd: " + rd + " ");
            System.out.println("fucnt3: " + funct3 + " ");
            System.out.println("rs1: " + rs1 + " ");
            System.out.println("rs2: " + rs2 + " ");
            System.out.println("imm: " + imm + " ");

            switch (opcode) {
            // fence, fence.i, ebreak, csrrw, csrrs, csrrc, csrrwi,
            // csrrsi, csrrci can be ignored

            case 0x0: // template
                switch (funct3) {
                }
                break;

            case 0x37: // LUI load upper immediate (20bits)
                System.out.println("U_imm: " + (U_imm << 12));
                reg[rd] = (U_imm << 12);
                break;

            case 0x27: // AUIPC Add Upper Imm (20bits) to PC
                reg[1] = pc; // do i need this?
                pc = (U_imm << 12);
                reg[rd] = pc;
                break;

            case 0x6f: // JAL
                System.out.println("J_imm: " + J_imm);
                System.out.println("pc: " + pc);
                reg[rd] = pc + 1;
                pc = pc + J_imm - 1;
                System.out.println("pc: " + pc);
                break;

            case 0x67: // JALR
                System.out.println("J_imm: " + J_imm);
                System.out.println("reg[rs1]: " + reg[rs1]);
                System.out.println("pc: " + pc);
                reg[rd] = pc + 1;
                pc = reg[rs1] + J_imm - 1;
                System.out.println("pc: " + pc);
                break;

            case 0x63: // type: branch
                System.out.println("B_imm: " + B_imm);
                System.out.println("pc: " + pc);
                switch (funct3) {
                case 0b000:// BEQ
                    if (reg[rs1] == reg[rs2]) {
                        reg[1] = pc + 1;
                        pc = pc + B_imm - 1;
                    }
                    break;
                case 0b001:// BNE
                    if (reg[rs1] != reg[rs2]) {
                        reg[1] = pc + 1;
                        pc = pc + B_imm - 1;
                    }
                    break;
                case 0b100: // BLT
                    if (reg[rs1] < reg[rs2]) {
                        reg[1] = pc + 1;
                        pc = pc + B_imm - 1;
                    }
                    break;
                case 0b101: // BGE
                    if (reg[rs1] >= reg[rs2]) {
                        reg[1] = pc + 1;
                        pc = pc + B_imm - 1;
                    }
                    break;

                case 0b110: // BLTU
                    if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
                        reg[1] = pc + 1;
                        pc = pc + B_imm - 1;
                    }
                    break;
                case 0b111:// BGEU
                    if (getUnsignedInt(reg[rs1]) < getUnsignedInt(reg[rs2])) {
                        reg[1] = pc + 1;
                        pc = pc + B_imm - 1;
                    }
                    break;
                }
                System.out.println("pc: " + pc);
                break;

            case 0x3: // type:load
                System.out.println("I_imm: " + I_imm);
                byte[] b = new byte[4];
                switch (funct3) {
                case 0b000: // LB
                    b[0] = data[reg[rs1] + I_imm];
                    b[1] = 0;
                    b[2] = 0;
                    b[3] = 0;
                    reg[rd] = byteToInt(b);
                    if ((b[0] >> 7) == 1) {
                        reg[rd] = reg[rd] * -1;
                    }
                    break;

                case 0b001: // LH
                    b[0] = data[reg[rs1] + I_imm];
                    b[1] = data[reg[rs1] + I_imm + 1];
                    b[2] = 0;
                    b[3] = 0;
                    reg[rd] = byteToInt(b);
                    if ((b[1] >> 7) == 1) {
                        reg[rd] = reg[rd] * -1;
                    }
                    break;

                case 0b010: // LW
                    //if (rs1 == 2) {
                      //  reg[rd] = (Integer) stack.pop();
                   // } else {
                        b[0] = data[reg[rs1] + I_imm];
                        b[1] = data[reg[rs1] + I_imm + 1];
                        b[2] = data[reg[rs1] + I_imm + 2];
                        b[3] = data[reg[rs1] + I_imm + 3];
                        reg[rd] = byteToInt(b);
                        //if ((b[3] >> 7) == 1) {
                           // reg[rd] = reg[rd] * -1;
                       // }
                    //}
                    break;

                case 0b100: // LBU
                    b[0] = data[reg[rs1] + I_imm];
                    b[1] = 0;
                    b[2] = 0;
                    b[3] = 0;
                    reg[rd] = byteToInt(b);
                    break;

                case 0b101: // LHU
                    b[0] = data[reg[rs1] + I_imm];
                    b[1] = data[reg[rs1] + I_imm + 1];
                    b[2] = 0;
                    b[3] = 0;
                    reg[rd] = byteToInt(b);
                    break;
                }
                break;

            case 0x23: // type: store
                System.out.println("S_imm: " + S_imm);
                byte[] temp = new byte[4];
                switch (funct3) {
                case 0b000: // SB

                    temp = intTo4Byte(reg[rs2]);
                    data[reg[rs1] + S_imm] = temp[3];
                    break;
                case 0b001: // SH
                    temp = intTo4Byte(reg[rs2]);
                    data[reg[rs1] + S_imm] = temp[2];
                    data[reg[rs1] + S_imm + 1] = temp[3];
                    break;
                case 0b010: // SW
                    //if (rs1 == 2) {
                      //  stack.push(reg[rs2]);
                    //} else {
                        temp = intTo4Byte(reg[rs2]);
                        data[reg[rs1] + S_imm] = temp[0];
                        data[reg[rs1] + S_imm + 1] = temp[1];
                        data[reg[rs1] + S_imm + 2] = temp[2];
                        data[reg[rs1] + S_imm + 3] = temp[3];
                    }
                //}
                break;

            case 0x13: // type: immediate
                System.out.println("I_imm: " + I_imm);
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
                        // shifting in java keep the sign,
                        // need checking
                        reg[rd] = reg[rs1] >> I_imm;
                        reg[rd] = (reg[rd] & 0xffffffff);
                    }
                    break;
                }
                break;

            case 0x33:
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

            case 0x73: // ecall
                pc = instructions.length;
                // When the program ends with ecall you write another binary file containing the
                // content of your registers (the .res file).
                // write it back out to a different file name
                System.out.println("ecall 10");
                SmallBinaryFiles writeBin = new SmallBinaryFiles();

                byte[] writeBinFile = regArrayToByteArray(reg);
                log("write file: " + OUTPUT_FILE_NAME);
                writeBin.writeSmallBinaryFile(writeBinFile, OUTPUT_FILE_NAME);

                break;

            default:
                System.out.println("Opcode 0x" + decToHex(opcode) + " not yet implemented");
                break;
            }

            ++pc; // We count in 4 byte words

            for (int i = 0; i < reg.length; ++i) {
                // System.out.print(decToHex(reg[i]) + " ");
                System.out.print(i + ":" + reg[i] + " ");
            }
            System.out.println();
            if (pc >= instructions.length) {
                break;
            }
        }

        System.out.println("Program exit");

    }

}
