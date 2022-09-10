package stdlib

import (
	"bufio"
	"bytes"
	"fmt"
	"io"
	"net"
	"oasisgo/core"
	"os"
)

type IO struct{}

func CreateSocket(socket net.Conn) *core.Prototype {
	return &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"__socket": socket,
			"port":     socket.RemoteAddr(),
			"close": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					err := socket.Close()
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, nil, false, "")
				},
			},
			"read": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					var buffer = make([]byte, args[0].(int))
					var n, err = socket.Read(buffer)
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					var result = make([]any, n)
					for i := 0; i < n; i++ {
						result[i] = buffer[i]
					}
					return CreateResult(vm, core.CreateOasisList(result), false, "")
				},
				Args: 1,
			},
			"write": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					_, err := socket.Write([]byte(args[0].(string)))
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, nil, false, "")
				},
				Args: 1,
			},
			"writeBytes": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					var buffer = bytes.NewBuffer(nil)
					for _, b := range args[0].([]any) {
						buffer.WriteByte(byte(b.(int)))
					}
					_, err := socket.Write(buffer.Bytes())
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, nil, false, "")
				},
				Args: 1,
			},
		},
	}
}

func (IO) Create(vm *core.VM) (string, any) {
	return "io", &core.Prototype{
		Inherited: &core.BasePrototype,
		Body: map[string]any{
			"print": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					fmt.Println(args[0])
					return nil
				},
				Args: 1,
			},
			"printraw": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					fmt.Printf("%s", args[0])
					return nil
				},
				Args: 1,
			},
			"read": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					fmt.Printf("%s", args[0])
					var buffer = bufio.NewReader(os.Stdin)
					var line, err = buffer.ReadString('\n')
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, line[:len(line)-1], false, "")
				},
				Args: 1,
			},
			"readChar": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					var buffer = bufio.NewReader(os.Stdin)
					var char, err = buffer.ReadByte()
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, string(char), false, "")
				},
				Args: 0,
			},
			"copy": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					var source = args[0].(string)
					var destination = args[1].(string)
					var sourceFile, err = os.Open(source)
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					defer sourceFile.Close()
					var destinationFile, err2 = os.Create(destination)
					if err2 != nil {
						return CreateResult(vm, nil, true, err2.Error())
					}
					defer destinationFile.Close()
					var _, err3 = io.Copy(destinationFile, sourceFile)
					if err3 != nil {
						return CreateResult(vm, nil, true, err3.Error())
					}
					return CreateResult(vm, nil, false, "")
				},
				Args: 2,
			},
			"create": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					file, err := os.Create(args[0].(string))
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, file, false, "")
				},
				Args: 1,
			},
			"createDir": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					err := os.Mkdir(args[0].(string), os.ModePerm)
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm, nil, false, "")
				},
				Args: 1,
			},
			"open": &core.NativeFunction{
				Fn: func(vm *core.VM, args []any) any {
					var file, err = os.OpenFile(args[0].(string), os.O_RDWR, os.ModePerm)
					if err != nil {
						return CreateResult(vm, nil, true, err.Error())
					}
					return CreateResult(vm,
						&core.Prototype{
							Inherited: &core.BasePrototype,
							Body: map[string]any{
								"__file": file,
								"close": &core.NativeFunction{
									Fn: func(vm *core.VM, args []any) any {
										err := file.Close()
										if err != nil {
											return CreateResult(vm, nil, true, err.Error())
										}
										return CreateResult(vm, nil, false, "")
									},
									Args: 0,
								},
								"readTo": &core.NativeFunction{
									Fn: func(vm *core.VM, args []any) any {
										var buffer = make([]byte, args[0].(int))
										var n, err = file.Read(buffer)
										if err != nil {
											return CreateResult(vm, nil, true, err.Error())
										}
										return CreateResult(vm, string(buffer[:n]), false, "")
									},
									Args: 1,
								},
								"read": &core.NativeFunction{
									Fn: func(vm *core.VM, args []any) any {
										var buffer = bytes.NewBuffer(nil)
										_, err := io.Copy(buffer, file)
										if err != nil {
											return CreateResult(vm, nil, true, err.Error())
										}
										return CreateResult(vm, buffer.String(), false, "")
									},
									Args: 0,
								},
								"readBytesTo": &core.NativeFunction{
									Fn: func(vm *core.VM, args []any) any {
										var buffer = make([]byte, args[0].(int))
										var n, err = file.Read(buffer)
										if err != nil {
											return CreateResult(vm, nil, true, err.Error())
										}
										var result = make([]any, n)
										for i := 0; i < n; i++ {
											result[i] = buffer[i]
										}
										return CreateResult(vm, core.CreateOasisList(result), false, "")
									},
									Args: 1,
								},
								"readBytes": &core.NativeFunction{
									Fn: func(vm *core.VM, args []any) any {
										var buffer = bytes.NewBuffer(nil)
										_, err := io.Copy(buffer, file)
										if err != nil {
											return CreateResult(vm, nil, true, err.Error())
										}
										var result = make([]any, buffer.Len())
										for i := 0; i < buffer.Len(); i++ {
											result[i] = buffer.Bytes()[i]
										}
										return CreateResult(vm, core.CreateOasisList(result), false, "")
									},
									Args: 0,
								},
								"write": &core.NativeFunction{
									Fn: func(vm *core.VM, args []any) any {
										_, err := file.WriteString(args[0].(string))
										if err != nil {
											return CreateResult(vm, nil, true, err.Error())
										}
										return CreateResult(vm, nil, false, "")
									},
									Args: 1,
								},
								"writeBytes": &core.NativeFunction{
									Fn: func(vm *core.VM, args []any) any {
										var buffer = bytes.NewBuffer(nil)
										for _, b := range args[0].([]any) {
											buffer.WriteByte(byte(b.(int)))
										}
										_, err := file.Write(buffer.Bytes())
										if err != nil {
											return CreateResult(vm, nil, true, err.Error())
										}
										return CreateResult(vm, nil, false, "")
									},
									Args: 1,
								},
							},
						}, false, "")
				},
				Args: 1,
			},
			"socket": &core.Prototype{
				Inherited: &core.BasePrototype,
				Body: map[string]any{
					"connect": &core.NativeFunction{
						Fn: func(vm *core.VM, args []any) any {
							var socket, err = net.Dial("tcp", args[0].(string))
							if err != nil {
								return CreateResult(vm, nil, true, err.Error())
							}
							return CreateResult(vm, CreateSocket(socket), false, "")
						},
						Args: 1,
					},
					"listen": &core.NativeFunction{
						Fn: func(vm *core.VM, args []any) any {
							var socket, err = net.Listen("tcp", args[0].(string))
							if err != nil {
								return CreateResult(vm, nil, true, err.Error())
							}
							return CreateResult(vm, &core.Prototype{
								Inherited: &core.BasePrototype,
								Body: map[string]any{
									"__socket": socket,
									"port":     socket.Addr(),
									"close": &core.NativeFunction{
										Fn: func(vm *core.VM, args []any) any {
											err := socket.Close()
											if err != nil {
												return CreateResult(vm, nil, true, err.Error())
											}
											return CreateResult(vm, nil, false, "")
										},
										Args: 0,
									},
									"accept": &core.NativeFunction{
										Fn: func(vm *core.VM, args []any) any {
											var socket, err = socket.Accept()
											if err != nil {
												return CreateResult(vm, nil, true, err.Error())
											}
											return CreateResult(vm, CreateSocket(socket), false, "")
										},
										Args: 0,
									},
								},
							}, false, "")
						},
						Args: 1,
					},
				},
			},
		},
	}
}
