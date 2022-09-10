// Code generated by "stringer -type=TokenType"; DO NOT EDIT.

package core

import "strconv"

func _() {
	// An "invalid array index" compiler error signifies that the constant values have changed.
	// Re-run the stringer command to generate them again.
	var x [1]struct{}
	_ = x[LeftParen-0]
	_ = x[RightParen-1]
	_ = x[Comma-2]
	_ = x[Colon-3]
	_ = x[Minus-4]
	_ = x[Plus-5]
	_ = x[Slash-6]
	_ = x[Star-7]
	_ = x[LeftBracket-8]
	_ = x[RightBracket-9]
	_ = x[LeftBrace-10]
	_ = x[RightBrace-11]
	_ = x[Bang-12]
	_ = x[BangEqual-13]
	_ = x[Pipe-14]
	_ = x[Equal-15]
	_ = x[EqualEqual-16]
	_ = x[Greater-17]
	_ = x[GreaterEqual-18]
	_ = x[Less-19]
	_ = x[LessEqual-20]
	_ = x[Mod-21]
	_ = x[PlusEqual-22]
	_ = x[MinusEqual-23]
	_ = x[SlashEqual-24]
	_ = x[StarEqual-25]
	_ = x[ModEqual-26]
	_ = x[RightPipe-27]
	_ = x[LeftPipe-28]
	_ = x[Question-29]
	_ = x[LambdaArrow-30]
	_ = x[Identifier-31]
	_ = x[String-32]
	_ = x[Float-33]
	_ = x[Int-34]
	_ = x[Char-35]
	_ = x[Byte-36]
	_ = x[Let-37]
	_ = x[Proto-38]
	_ = x[Fn-39]
	_ = x[For-40]
	_ = x[If-41]
	_ = x[Nil-42]
	_ = x[Return-43]
	_ = x[True-44]
	_ = x[False-45]
	_ = x[While-46]
	_ = x[End-47]
	_ = x[Else-48]
	_ = x[And-49]
	_ = x[Not-50]
	_ = x[Or-51]
	_ = x[Clone-52]
	_ = x[Spawn-53]
	_ = x[Send-54]
	_ = x[Recv-55]
	_ = x[Export-56]
	_ = x[Map-57]
	_ = x[Item-58]
	_ = x[Is-59]
	_ = x[Const-60]
	_ = x[In-61]
	_ = x[Break-62]
	_ = x[Continue-63]
	_ = x[Of-64]
	_ = x[Begin-65]
	_ = x[Until-66]
	_ = x[Unless-67]
	_ = x[Eof-68]
}

const _TokenType_name = "LeftParenRightParenCommaColonMinusPlusSlashStarLeftBracketRightBracketLeftBraceRightBraceBangBangEqualPipeEqualEqualEqualGreaterGreaterEqualLessLessEqualModPlusEqualMinusEqualSlashEqualStarEqualModEqualRightPipeLeftPipeQuestionLambdaArrowIdentifierStringFloatIntCharByteLetProtoFnForIfNilReturnTrueFalseWhileEndElseAndNotOrCloneSpawnSendRecvExportMapItemIsConstInBreakContinueOfBeginUntilUnlessEof"

var _TokenType_index = [...]uint16{0, 9, 19, 24, 29, 34, 38, 43, 47, 58, 70, 79, 89, 93, 102, 106, 111, 121, 128, 140, 144, 153, 156, 165, 175, 185, 194, 202, 211, 219, 227, 238, 248, 254, 259, 262, 266, 270, 273, 278, 280, 283, 285, 288, 294, 298, 303, 308, 311, 315, 318, 321, 323, 328, 333, 337, 341, 347, 350, 354, 356, 361, 363, 368, 376, 378, 383, 388, 394, 397}

func (i TokenType) String() string {
	if i < 0 || i >= TokenType(len(_TokenType_index)-1) {
		return "TokenType(" + strconv.FormatInt(int64(i), 10) + ")"
	}
	return _TokenType_name[_TokenType_index[i]:_TokenType_index[i+1]]
}