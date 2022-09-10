package stdlib

import (
	"math"
	"oasisgo/core"
)

type Math struct{}

func (Math) Create(vm *core.VM) (string, any) {
	return "math", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"fAbs": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Abs(args[0].(float64))
				},
				Args: 1,
			},
			"abs": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Abs(float64(args[0].(int)))
				},
				Args: 1,
			},
			"iAbs": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return int(math.Abs(float64(args[0].(int))))
				},
			},
			"fAcos": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Acos(args[0].(float64))
				},
				Args: 1,
			},
			"acos": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Acos(float64(args[0].(int)))
				},
				Args: 1,
			},
			"fAsin": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Asin(args[0].(float64))
				},
				Args: 1,
			},
			"asin": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Asin(float64(args[0].(int)))
				},
				Args: 1,
			},
			"fAtan": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Atan(args[0].(float64))
				},
				Args: 1,
			},
			"atan": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Atan(float64(args[0].(int)))
				},
				Args: 1,
			},
			"fAtan2": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Atan2(args[0].(float64), args[1].(float64))
				},
				Args: 2,
			},
			"atan2": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Atan2(float64(args[0].(int)), float64(args[1].(int)))
				},
				Args: 2,
			},
			"fCeil": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Ceil(args[0].(float64))
				},
				Args: 1,
			},
			"ceil": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Ceil(float64(args[0].(int)))
				},
				Args: 1,
			},
			"fCos": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Cos(args[0].(float64))
				},
				Args: 1,
			},
			"cos": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Cos(float64(args[0].(int)))
				},
				Args: 1,
			},
			"fExp": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Exp(args[0].(float64))
				},
				Args: 1,
			},
			"exp": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Exp(float64(args[0].(int)))
				},
				Args: 1,
			},
			"fFloor": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Floor(args[0].(float64))
				},
				Args: 1,
			},
			"floor": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Floor(float64(args[0].(int)))
				},
				Args: 1,
			},
			"fLog": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Log(args[0].(float64))
				},
				Args: 1,
			},
			"log": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Log(float64(args[0].(int)))
				},
			},
			"fLog10": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Log10(args[0].(float64))
				},
			},
			"log10": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Log10(float64(args[0].(int)))
				},
			},
			"fPow": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Pow(args[0].(float64), args[1].(float64))
				},
			},
			"pow": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Pow(float64(args[0].(int)), float64(args[1].(int)))
				},
			},
			"fRound": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Round(args[0].(float64))
				},
			},
			"round": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Round(float64(args[0].(int)))
				},
			},
			"fSin": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Sin(args[0].(float64))
				},
			},
			"sin": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Sin(float64(args[0].(int)))
				},
			},
			"fSqrt": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Sqrt(args[0].(float64))
				},
			},
			"sqrt": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Sqrt(float64(args[0].(int)))
				},
			},
			"fTan": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Tan(args[0].(float64))
				},
			},
			"tan": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Tan(float64(args[0].(int)))
				},
			},
			"fTrunc": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Trunc(args[0].(float64))
				},
			},
			"trunc": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return math.Trunc(float64(args[0].(int)))
				},
			},
		},
	}
}
