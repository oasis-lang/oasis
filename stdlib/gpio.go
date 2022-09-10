package stdlib

import (
	"github.com/stianeikeland/go-rpio"
	"oasisgo/core"
)

type GPIO struct{}

func (GPIO) Create(vm *core.VM) (string, any) {
	return "gpio", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"init": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					err := rpio.Open()
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, nil, false, "")
				},
				Args: 0,
			},
			"done": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					err := rpio.Close()
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, nil, false, "")
				},
				Args: 0,
			},
			"pin": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					pin := rpio.Pin(args[0].(int))
					return &core.Prototype{
						Inherited: &core.BasePrototype,
						Body: map[string]any{
							"__pin": pin,
							"input": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.Input()
									return nil
								},
								Args: 0,
							},
							"output": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.Output()
									return nil
								},
								Args: 0,
							},
							"high": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.High()
									return nil
								},
								Args: 0,
							},
							"low": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.Low()
									return nil
								},
								Args: 0,
							},
							"read": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									val := pin.Read()
									if val == rpio.High {
										return true
									}
									return false
								},
								Args: 0,
							},
							"write": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									if args[0].(bool) {
										pin.Write(rpio.High)
									} else {
										pin.Write(rpio.Low)
									}
									return nil
								},
								Args: 1,
							},
							"pwm": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.Pwm()
									return nil
								},
								Args: 0,
							},
							"freq": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.Freq(args[0].(int))
									return nil
								},
								Args: 1,
							},
							"dutyCycle": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.DutyCycle(uint32(args[0].(int)), uint32(args[1].(int)))
									return nil
								},
								Args: 2,
							},
							"toggle": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									pin.Toggle()
									return nil
								},
								Args: 0,
							},
						},
					}
				},
				Args: 1,
			},
		},
	}
}
