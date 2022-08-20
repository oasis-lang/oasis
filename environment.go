package main

import (
	"errors"
	"fmt"
)

type OasisEnvironment struct {
	Parent *OasisEnvironment
	Values map[string]interface{}
	Consts []string
}

func NewEnvironment(parent *OasisEnvironment) *OasisEnvironment {
	return &OasisEnvironment{
		Parent: parent,
		Values: map[string]interface{}{},
		Consts: []string{},
	}
}

func (e *OasisEnvironment) DefineVariable(name string, isConst bool, value interface{}) error {
	if e.Has(name) {
		return errors.New(fmt.Sprintf("Variable already defined: %s", name))
	} else {
		e.Values[name] = value
		if isConst {
			e.Consts = append(e.Consts, name)
		}
	}
	return nil
}

func (e *OasisEnvironment) Get(name string) (interface{}, error) {
	if value, ok := e.Values[name]; ok {
		return value, nil
	} else if e.Parent != nil {
		return e.Parent.Get(name)
	}
	return nil, errors.New(fmt.Sprintf("Undefined variable: %s", name))
}

func (e *OasisEnvironment) Set(name string, value interface{}) error {
	if e.Has(name) {
		if contains(e.Consts, name) {
			return errors.New(fmt.Sprintf("Variable is const: %s", name))
		}
		e.Values[name] = value
	} else {
		if e.Parent != nil {
			return e.Parent.Set(name, value)
		} else {
			return errors.New(fmt.Sprintf("Undefined variable: %s", name))
		}
	}
	return nil
}

func (e *OasisEnvironment) Has(name string) bool {
	if _, ok := e.Values[name]; ok {
		return true
	} else if e.Parent != nil {
		return e.Parent.Has(name)
	} else {
		return false
	}
}

func contains(elems []string, v string) bool {
	for _, s := range elems {
		if v == s {
			return true
		}
	}
	return false
}
