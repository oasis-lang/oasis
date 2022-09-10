package core

import (
	"errors"
	"fmt"
	"reflect"
)

type OasisIterator struct {
	Source    any
	Current   int
	Exhausted bool
}

func NewOasisIterator(source any) OasisIterator {
	return OasisIterator{
		Source:    source,
		Current:   -1,
		Exhausted: false,
	}
}

func (i *OasisIterator) Next(vm *VM) (error, any) {
	if i.Exhausted {
		return errors.New("Iterator exhausted"), nil
	}
	i.Current++
	switch i.Source.(type) {
	case OasisList:
		if i.Current >= len(*(i.Source.(OasisList)))-1 {
			i.Exhausted = true
		}
		return nil, (*(i.Source.(OasisList)))[i.Current]
	case OasisMap:
		if i.Current >= len(i.Source.(OasisMap))-1 {
			i.Exhausted = true
		}
		var keys = reflect.ValueOf(i.Source).MapKeys()
		var key = keys[i.Current].Interface()
		var value = reflect.ValueOf(i.Source).MapIndex(reflect.ValueOf(key)).Interface()
		return nil, CreateOasisTuple([]any{key, value})
	case Tuple:
		if i.Current >= len(i.Source.(Tuple).Values)-1 {
			i.Exhausted = true
		}
		return nil, i.Source.(Tuple).Values[i.Current]
	case *Prototype:
		if proto, ok := i.Source.(*Prototype); ok {
			if fn, ok := proto.Get("__iter"); ok != nil {
				if fn, ok := fn.(OasisCallable); ok {
					var item = fn.Call(vm, []any{i.Current})
					if item == nil {
						i.Exhausted = true
						return errors.New("Iterator exhausted"), nil
					}
					return nil, item
				}
			}
		}
	}
	return errors.New("Invalid iterable"), nil
}

func (i *OasisIterator) CurrentValue() any {
	return reflect.ValueOf(i.Source).Index(i.Current).Interface()
}

func (i *OasisIterator) CurrentIndex() int {
	return i.Current
}

func (i *OasisIterator) Reset() {
	i.Current = 0
	i.Exhausted = false
}

func (i *OasisIterator) String() string {
	return fmt.Sprintf("Iterator(Source=%v, Current=%d, Exhausted=%v)", i.Source, i.Current, i.Exhausted)
}
