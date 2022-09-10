package core

import "fmt"

type OasisEnvironment struct {
	Values map[string]any
	Consts []string
}

func NewEnvironment() OasisEnvironment {
	return OasisEnvironment{
		Values: map[string]any{},
		Consts: []string{},
	}
}

func (e *OasisEnvironment) Define(name string, isConst bool, value any) error {
	if _, ok := e.Values[name]; ok {
		return fmt.Errorf("Variable %s already defined", name)
	}
	e.Values[name] = value
	if isConst {
		e.Consts = append(e.Consts, name)
	}
	return nil
}

func (e *OasisEnvironment) Assign(name string, value any) error {
	if _, ok := e.Values[name]; !ok {
		return fmt.Errorf("Variable %s not defined", name)
	}
	e.Values[name] = value
	return nil
}

func (e *OasisEnvironment) Get(name string) (any, error) {
	if _, ok := e.Values[name]; !ok {
		return nil, fmt.Errorf("Variable %s not defined", name)
	}
	return e.Values[name], nil
}
