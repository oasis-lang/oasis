package main

import (
	"errors"
	"fmt"
)

type Prototype struct {
	Inherited *Prototype
	Body      map[string]interface{}
}

func NewPrototype(inherited *Prototype) *Prototype {
	return &Prototype{
		Inherited: inherited,
		Body:      make(map[string]interface{}),
	}
}

func (p *Prototype) Set(name string, value interface{}) {
	p.Body[name] = value
}

func (p *Prototype) Get(name string) (interface{}, error) {
	if value, ok := p.Body[name]; ok {
		return value, nil
	} else if p.Inherited != nil {
		return p.Inherited.Get(name)
	}
	return nil, errors.New(fmt.Sprintf("Prototype does not contain %s", name))
}
