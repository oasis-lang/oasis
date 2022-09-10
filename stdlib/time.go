package stdlib

import (
	"oasisgo/core"
	"time"
)

type Time struct{}

func (Time) Create(vm *core.VM) (string, any) {
	return "time", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"clock": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					return time.Now().Unix()
				},
				Args: 0,
			},
			"sleep": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					time.Sleep(time.Duration(args[0].(int)) * time.Second)
					return nil
				},
				Args: 1,
			},
			"sleepMs": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					time.Sleep(time.Duration(args[0].(int)) * time.Millisecond)
					return nil
				},
				Args: 1,
			},
			"now": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					var now = time.Now()
					return &core.Prototype{
						Inherited: &core.BasePrototype,
						Body: map[string]any{
							"__now":       now,
							"toString":    now.String(),
							"year":        now.Year(),
							"month":       int(now.Month()),
							"day":         now.Day(),
							"hour":        now.Hour(),
							"minute":      now.Minute(),
							"second":      now.Second(),
							"millisecond": now.Nanosecond() / 1000000,
							"microsecond": now.Nanosecond() / 1000,
							"nanosecond":  now.Nanosecond(),
						},
					}
				},
				Args: 0,
			},
		},
	}
}
