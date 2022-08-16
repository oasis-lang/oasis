package main

import (
	"errors"
	"fmt"
)

type OasisEnvironment struct {
	Parent *OasisEnvironment
	Values map[string]interface{}
}

func NewEnvironment(parent *OasisEnvironment) *OasisEnvironment {
	return &OasisEnvironment{
		Parent: parent,
		Values: make(map[string]interface{}),
	}
}

func (e *OasisEnvironment) DefineVariable(name string, value interface{}) error {
	if e.Has(name) {
		return errors.New(fmt.Sprintf("Variable already defined: %s", name))
	} else {
		e.Values[name] = value
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
	if e.Values[name] != nil {
		e.Values[name] = value
	} else {
		return errors.New(fmt.Sprintf("Undefined variable: %s", name))
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
