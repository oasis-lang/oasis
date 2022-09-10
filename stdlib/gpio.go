package stdlib

import (
	"gobot.io/x/gobot"
	"gobot.io/x/gobot/drivers/gpio"
	"gobot.io/x/gobot/platforms/raspi"
	"oasisgo/core"
)

type GPIO struct{}

func (GPIO) Create(vm *core.VM) (string, any) {
	r := raspi.NewAdaptor()
	return "GPIO", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"init": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return &core.Prototype{
						Inherited: &core.BasePrototype,
						Body: map[string]any{
							"__raspi": r,
							"led": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									led := gpio.NewLedDriver(r, args[0].(string))
									return &core.Prototype{
										Inherited: &core.BasePrototype,
										Body: map[string]any{
											"__led": led,
											"__add_to_bot": func() gobot.Device {
												return led
											},
											"on": &core.NativeFunction{
												Fn: func(vm *core.VM, args []any) any {
													err := led.On()
													if err != nil {
														return CreateResult(vm, nil, true, err.Error())
													}
													return CreateResult(vm, nil, false, "")
												},
												Args: 0,
											},
											"off": &core.NativeFunction{
												Fn: func(vm *core.VM, args []any) any {
													err := led.Off()
													if err != nil {
														return CreateResult(vm, nil, true, err.Error())
													}
													return CreateResult(vm, nil, false, "")
												},
												Args: 0,
											},
										},
									}
								},
								Args: 1,
							},
							"button": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									button := gpio.NewButtonDriver(r, args[0].(string))
									return &core.Prototype{
										Inherited: &core.BasePrototype,
										Body: map[string]any{
											"__button": button,
											"__add_to_bot": func() gobot.Device {
												return button
											},
											"on": &core.NativeFunction{
												Fn: func(vm *core.VM, args []any) any {
													err := button.On(args[0].(string), func(data interface{}) {
														args[1].(core.OasisCallable).Call(vm, []any{data})
													})
													if err != nil {
														return CreateResult(vm, nil, true, err.Error())
													}
													return CreateResult(vm, nil, false, "")
												},
												Args: 2,
											},
										},
									}
								},
								Args: 1,
							},
							"servo": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									servo := gpio.NewServoDriver(r, args[0].(string))
									return &core.Prototype{
										Inherited: &core.BasePrototype,
										Body: map[string]any{
											"__servo": servo,
											"__add_to_bot": func() gobot.Device {
												return servo
											},
											"move": &core.NativeFunction{
												Fn: func(vm *core.VM, args []any) any {
													err := servo.Move(uint8(args[0].(int)))
													if err != nil {
														return CreateResult(vm, nil, true, err.Error())
													}
													return CreateResult(vm, nil, false, "")
												},
												Args: 1,
											},
											"min": &core.NativeFunction{
												Fn: func(vm *core.VM, args []any) any {
													err := servo.Min()
													if err != nil {
														return CreateResult(vm, nil, true, err.Error())
													}
													return CreateResult(vm, nil, false, "")
												},
												Args: 0,
											},
											"max": &core.NativeFunction{
												Fn: func(vm *core.VM, args []any) any {
													err := servo.Max()
													if err != nil {
														return CreateResult(vm, nil, true, err.Error())
													}
													return CreateResult(vm, nil, false, "")
												},
												Args: 0,
											},
											"center": &core.NativeFunction{
												Fn: func(vm *core.VM, args []any) any {
													err := servo.Center()
													if err != nil {
														return CreateResult(vm, nil, true, err.Error())
													}
													return CreateResult(vm, nil, false, "")
												},
												Args: 0,
											},
										},
									}
								},
							},
						},
					}
				},
			},
			"create": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					devicesRaw := args[1].(core.OasisList)
					devices := make([]gobot.Device, len(*devicesRaw))
					for i, deviceRaw := range *devicesRaw {
						device, ok := deviceRaw.(*core.Prototype)
						if !ok {
							return CreateResult(vm, nil, true, "Expected device to be a prototype")
						}
						addToBot, err := device.Get("__add_to_bot")
						if err != nil {
							return CreateResult(vm, nil, true, "Not a device")
						}
						addToBotFn, ok := addToBot.(func() gobot.Device)
						if !ok {
							return CreateResult(vm, nil, true, "Not a device")
						}
						devices[i] = addToBotFn()
					}
					bot := gobot.NewRobot(args[0].(string),
						[]gobot.Connection{r},
						devices,
						func() {
							select {}
						},
					)
					return &core.Prototype{
						Inherited: &core.BasePrototype,
						Body: map[string]any{
							"__bot": bot,
							"start": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									err := bot.Start()
									if err != nil {
										return CreateResult(vm, nil, true, err.Error())
									}
									return CreateResult(vm, nil, false, "")
								},
								Args: 0,
							},
							"stop": &core.NativeFunction{
								Fn: func(vm *core.VM, args []any) any {
									err := bot.Stop()
									if err != nil {
										return CreateResult(vm, nil, true, err.Error())
									}
									return CreateResult(vm, nil, false, "")
								},
								Args: 0,
							},
						},
					}
				},
			},
		},
	}
}
